package egovframework.example.global.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Offline source review only; it does not parse or execute Oracle SQL. */
class OracleSchemaSourceTest {
    private static final Path DIR = Path.of("db", "oracle");
    private static final Pattern VERSION = Pattern.compile("V(\\d{3})__[a-z0-9_]+\\.sql");
    private static final Pattern TABLE = Pattern.compile("(?im)^CREATE TABLE ([a-z_]+)\\s*\\(");
    private static final Pattern REFERENCE = Pattern.compile("(?i)\\bREFERENCES\\s+([a-z_]+)\\s*\\(");
    private static final Pattern CONSTRAINT = Pattern.compile("(?i)\\bCONSTRAINT\\s+([a-z_]+)");

    @Test
    void versionsAreOrderedAndAllSeventeenTablesExist() throws IOException {
        var scripts = migrationFiles();
        assertThat(scripts).extracting(path -> path.getFileName().toString())
                .containsExactly("V001__initial_schema.sql", "V002__indexes_guards_roles.sql");
        var versions = new HashSet<String>();
        for (Path script : scripts) {
            var matcher = VERSION.matcher(script.getFileName().toString());
            assertThat(matcher.matches()).isTrue();
            assertThat(versions.add(matcher.group(1))).isTrue();
        }
        var ddl = Files.readString(scripts.getFirst(), StandardCharsets.UTF_8);
        assertThat(captures(TABLE, ddl)).containsExactlyInAnyOrder(
                "department", "employee", "employee_account", "account_activation",
                "system_role", "employee_role", "work_task", "task_assignment",
                "chat_room", "chat_message", "chat_read_state", "document",
                "document_version", "document_review", "document_grant",
                "document_fragment", "audit_event");
        assertThat(ddl.indexOf("ALTER TABLE document ADD CONSTRAINT fk_doc_current_version"))
                .isGreaterThan(ddl.indexOf("CREATE TABLE document_version"));
        assertThat(ddl).contains("GENERATED ALWAYS AS IDENTITY", "CLOB", "TIMESTAMP(6) WITH TIME ZONE");
    }

    @Test
    void migrationHashesMatchTheVersionedManifest() throws Exception {
        var scripts = migrationFiles();
        var manifest = Files.readAllLines(DIR.resolve("manifest.sha256"), StandardCharsets.UTF_8);
        assertThat(manifest).hasSameSizeAs(scripts);
        var sha256 = MessageDigest.getInstance("SHA-256");
        for (int i = 0; i < scripts.size(); i++) {
            var parts = manifest.get(i).trim().split("\\s+");
            assertThat(parts).hasSize(2);
            assertThat(parts[1]).isEqualTo(scripts.get(i).getFileName().toString());
            assertThat(HexFormat.of().formatHex(sha256.digest(Files.readAllBytes(scripts.get(i)))))
                    .isEqualTo(parts[0]);
        }
    }

    @Test
    void referencesAndConstraintNamesAreInternallyConsistent() throws IOException {
        var ddl = Files.readString(DIR.resolve("V001__initial_schema.sql"), StandardCharsets.UTF_8);
        var tables = new HashSet<>(captures(TABLE, ddl));
        assertThat(tables).containsAll(captures(REFERENCE, ddl));
        var names = captures(CONSTRAINT, ddl);
        assertThat(new HashSet<>(names)).hasSameSizeAs(names);
        assertThat(names).allSatisfy(name -> assertThat(name.length()).isLessThanOrEqualTo(30));
        assertThat(ddl).contains("UNIQUE (employee_low_id, employee_high_id)",
                "CHECK (employee_low_id < employee_high_id)",
                "UNIQUE (room_id, room_seq)", "UNIQUE (sender_id, client_request_id)",
                "PRIMARY KEY (room_id, employee_id)",
                "FOREIGN KEY (document_id, current_version_id)");
    }

    @Test
    void sourceHasNoAiTranscriptTableOrDestructiveStatement() throws IOException {
        String sql = Files.readString(DIR.resolve("V001__initial_schema.sql"), StandardCharsets.UTF_8)
                + Files.readString(DIR.resolve("V002__indexes_guards_roles.sql"), StandardCharsets.UTF_8);
        assertThat(sql).doesNotContainPattern("(?im)^\\s*(DROP|TRUNCATE|DELETE FROM)\\b");
        assertThat(sql).doesNotContainPattern("(?i)\\b(ai_question|ai_answer|gemini_key|api_key|prompt_text|raw_transcript)\\b");
        assertThat(sql).contains("ai_searchable", "external_ai_allowed", "ck_fragment_external",
                "trg_version_immutable", "trg_fragment_immutable");
        assertThat(sql).contains("CREATE TABLE audit_event");
        assertThat(sql).doesNotContainPattern("(?is)CREATE TABLE audit_event\\s*\\([^;]*\\bCLOB\\b");
    }

    @Test
    void manualRunnerChecksEmptySchemaAndNeverDropsIt() throws IOException {
        String apply = Files.readString(DIR.resolve("apply.sql"), StandardCharsets.UTF_8);
        String preflight = Files.readString(DIR.resolve("verify_empty.sql"), StandardCharsets.UTF_8);
        String verification = Files.readString(DIR.resolve("verify_schema.sql"), StandardCharsets.UTF_8);
        assertThat(apply).contains("WHENEVER SQLERROR EXIT SQL.SQLCODE", "@@verify_empty.sql",
                "@@V001__initial_schema.sql", "@@V002__indexes_guards_roles.sql", "@@verify_schema.sql");
        assertThat(preflight).contains("FROM user_objects", "MIGRATION_REQUIRES_EMPTY_SCHEMA");
        assertThat(verification).contains("v_tables <> 17", "v_roles <> 5", "v_triggers <> 2");
    }

    @Test
    void jdbcMigrationParserKeepsTriggersAsSingleStatements() throws IOException {
        var first = OracleSchemaOracleIT.statements(
                Files.readAllLines(DIR.resolve("V001__initial_schema.sql"), StandardCharsets.UTF_8));
        var second = OracleSchemaOracleIT.statements(
                Files.readAllLines(DIR.resolve("V002__indexes_guards_roles.sql"), StandardCharsets.UTF_8));
        assertThat(first).hasSize(18);
        assertThat(first).anySatisfy(sql ->
                assertThat(sql).startsWith("ALTER TABLE document ADD CONSTRAINT fk_doc_current_version"));
        assertThat(second).hasSize(26);
        assertThat(second).filteredOn(sql -> sql.startsWith("CREATE OR REPLACE TRIGGER"))
                .hasSize(2).allSatisfy(sql -> assertThat(sql).endsWith("END;"));
    }

    private List<Path> migrationFiles() throws IOException {
        try (var paths = Files.list(DIR)) {
            return paths.filter(path -> path.getFileName().toString().startsWith("V"))
                    .sorted().toList();
        }
    }

    private List<String> captures(Pattern pattern, String text) {
        var values = new ArrayList<String>();
        var matcher = pattern.matcher(text);
        while (matcher.find()) values.add(matcher.group(1).toLowerCase());
        return values;
    }
}
