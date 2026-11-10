package egovframework.example.global.config;

import egovframework.example.auth.fixture.SyntheticAccountFixture;
import egovframework.example.document.fixture.SyntheticDocumentFixture;
import egovframework.example.organization.fixture.SyntheticOrganizationFixture;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/** Deterministic Oracle-only seed renderer from the same PSV files used by test. */
public final class SyntheticOracleSeedRenderer {
    private SyntheticOracleSeedRenderer() { }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Expected seed SQL output path");
        Files.writeString(Path.of(args[0]), render(), StandardCharsets.UTF_8);
    }

    public static String render() {
        var sql = new StringBuilder("-- Oracle 26ai synthetic fixture seed. Generated from src/main/resources/fixtures.\n"
                + "-- Accounts are PENDING with an unusable marker, never a committed password or activation code.\n"
                + "-- Run only through seed/apply_seed.sql in a dedicated synthetic schema.\n\n");

        for (var department : SyntheticOrganizationFixture.departments()) {
            merge(sql, "department", "t.department_code = s.department_code",
                    "SELECT " + q(department.departmentCode()) + " department_code, "
                            + q(department.departmentName()) + " department_name FROM dual",
                    "department_code, department_name", "s.department_code, s.department_name");
        }
        for (var employee : SyntheticOrganizationFixture.employees()) {
            merge(sql, "employee", "t.employee_code = s.employee_code",
                    "SELECT " + q(employee.employeeId()) + " employee_code, "
                            + q(employee.displayName()) + " display_name, d.department_id, "
                            + q(employee.jobTitle()) + " job_title, " + q(employee.extensionNo()) + " extension_no, "
                            + q(employee.companyEmail()) + " company_email, "
                            + q(employee.dutySummary()) + " duty_summary FROM department d WHERE d.department_code = "
                            + q(employee.departmentCode()),
                    "employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary",
                    "s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary");
        }
        for (var account : SyntheticAccountFixture.accounts()) {
            merge(sql, "employee_account", "t.employee_id = s.employee_id",
                    "SELECT e.employee_id, " + q(account.loginId())
                            + " login_id FROM employee e WHERE e.employee_code = " + q(account.employeeCode()),
                    "employee_id, login_id, password_hash, account_status",
                    "s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING'");
            account.roles().stream().sorted(Comparator.comparing(Enum::name)).forEach(role ->
                    merge(sql, "employee_role", "t.employee_id = s.employee_id AND t.role_code = s.role_code",
                            "SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e "
                                    + "CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = "
                                    + q(account.employeeCode()) + " AND r.role_code = " + q(role.name())
                                    + " AND issuer.employee_code = 'EMP-001'",
                            "employee_id, role_code, assigned_by_employee_id",
                            "s.employee_id, s.role_code, s.issuer_id"));
        }

        for (var document : SyntheticDocumentFixture.documents()) {
            String departmentId = document.departmentCode() == null ? "NULL" :
                    "(SELECT department_id FROM department WHERE department_code = "
                            + q(document.departmentCode()) + ")";
            merge(sql, "document", "t.document_code = s.document_code",
                    "SELECT " + q(document.documentCode()) + " document_code, " + q(document.title())
                            + " document_title, " + q(document.category()) + " document_category, "
                            + "e.employee_id owner_employee_id, " + departmentId + " department_id, "
                            + q(document.scope().name()) + " document_scope FROM employee e WHERE e.employee_code = "
                            + q(document.ownerCode()),
                    "document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status",
                    "s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT'");

            merge(sql, "document_tag", "t.document_id = s.document_id AND t.tag_value = s.tag_value",
                    "SELECT d.document_id, " + q(document.category()) + " tag_value FROM document d WHERE d.document_code = "
                            + q(document.documentCode()),
                    "document_id, tag_value", "s.document_id, s.tag_value");

            String initialState = "PUBLISHED".equals(document.versionState()) ? "DRAFT" : document.versionState();
            merge(sql, "document_version", "t.document_id = s.document_id AND t.version_no = 1",
                    "SELECT d.document_id, e.employee_id creator_id, " + q(document.format())
                            + " content_format, " + q(document.body()) + " body_text FROM document d "
                            + "JOIN employee e ON e.employee_code = " + q(document.ownerCode())
                            + " WHERE d.document_code = " + q(document.documentCode()),
                    "document_id, version_no, content_format, body_text, version_state, created_by_employee_id",
                    "s.document_id, 1, s.content_format, s.body_text, " + q(initialState) + ", s.creator_id");

            merge(sql, "document_fragment", "t.version_id = s.version_id AND t.fragment_no = 1",
                    "SELECT v.version_id, " + q(document.body()) + " body_text FROM document d "
                            + "JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 "
                            + "WHERE d.document_code = " + q(document.documentCode()),
                    "version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn",
                    "s.version_id, 1, s.body_text, " + flag(document.aiSearchable()) + ", "
                            + flag(document.externalAiAllowed()) + ", " + flag(document.synthetic()) + ", "
                            + flag(document.nonSensitive()));

            document.readGrants().stream().sorted().forEach(employee -> grant(sql, document.documentCode(), employee, "READ"));
            document.approveGrants().stream().sorted().forEach(employee -> grant(sql, document.documentCode(), employee, "APPROVE"));

            if (document.reviewerCode() != null) {
                merge(sql, "document_review", "t.version_id = s.version_id AND t.decision_code = s.decision_code",
                        "SELECT d.document_id, v.version_id, e.employee_id reviewer_id, "
                                + q(document.reviewDecision()) + " decision_code FROM document d "
                                + "JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 "
                                + "JOIN employee e ON e.employee_code = " + q(document.reviewerCode())
                                + " WHERE d.document_code = " + q(document.documentCode()),
                        "document_id, version_id, reviewer_employee_id, decision_code",
                        "s.document_id, s.version_id, s.reviewer_id, s.decision_code");
            }

            if ("PUBLISHED".equals(document.versionState())) {
                sql.append("UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP "
                        + "WHERE v.version_state = 'DRAFT' AND v.document_id = "
                        + "(SELECT document_id FROM document WHERE document_code = ")
                        .append(q(document.documentCode())).append(") AND v.version_no = 1;\n\n");
            }
            sql.append("UPDATE document d SET current_version_id = "
                    + "(SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), "
                    + "document_status = ").append(q(document.documentStatus()))
                    .append(" WHERE d.document_code = ").append(q(document.documentCode()))
                    .append(" AND d.current_version_id IS NULL;\n\n");
        }
        return sql.toString();
    }

    private static void grant(StringBuilder sql, String documentCode, String employeeCode, String permission) {
        merge(sql, "document_grant", "t.document_id = s.document_id AND t.employee_id = s.employee_id "
                        + "AND t.permission_code = s.permission_code",
                "SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, " + q(permission)
                        + " permission_code FROM document d JOIN employee e ON e.employee_code = "
                        + q(employeeCode) + " WHERE d.document_code = " + q(documentCode),
                "document_id, employee_id, permission_code, granted_by_employee_id",
                "s.document_id, s.employee_id, s.permission_code, s.granter_id");
    }

    private static void merge(StringBuilder sql, String table, String condition, String source,
                              String columns, String values) {
        sql.append("MERGE INTO ").append(table).append(" t USING (\n    ").append(source)
                .append("\n) s ON (").append(condition)
                .append(")\nWHEN NOT MATCHED THEN INSERT (").append(columns)
                .append(") VALUES (").append(values).append(");\n\n");
    }

    private static String q(String value) { return "'" + value.replace("'", "''") + "'"; }
    private static String flag(boolean value) { return value ? "'Y'" : "'N'"; }
}
