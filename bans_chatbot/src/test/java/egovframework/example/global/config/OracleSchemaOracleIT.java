package egovframework.example.global.config;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Explicit Oracle-only suite. Never included in the default Maven test goal. */
@Tag("oracle-integration")
@SpringBootTest
@ActiveProfiles({"oracle", "oracle-it"})
@TestMethodOrder(OrderAnnotation.class)
class OracleSchemaOracleIT {
    private static final Path DIR = Path.of("db", "oracle");
    @Autowired DataSource dataSource;

    @Test
    @Order(1)
    void appliesMigrationsOnlyToExplicitlyAuthorizedEmptySchema() throws Exception {
        Assumptions.assumeTrue("YES".equals(System.getenv("ORACLE_EMPTY_SCHEMA_MIGRATION_TEST")),
                "Set ORACLE_EMPTY_SCHEMA_MIGRATION_TEST=YES only for a disposable empty schema");
        try (Connection connection = dataSource.getConnection()) {
            assertThat(scalar(connection, "SELECT COUNT(*) FROM user_objects")).isZero();
            for (String file : List.of("V001__initial_schema.sql", "V002__indexes_guards_roles.sql", "V003__chat_message_length_guard.sql")) {
                for (String statement : statements(Files.readAllLines(DIR.resolve(file), StandardCharsets.UTF_8))) {
                    try (Statement command = connection.createStatement()) {
                        command.execute(statement);
                    }
                }
            }
            assertSchemaMetadata(connection);
        }
    }

    @Test
    @Order(2)
    void migratedSchemaExistsAndRepeatPreflightRejectsIt() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertSchemaMetadata(connection);
            String preflight = Files.readAllLines(DIR.resolve("verify_empty.sql"), StandardCharsets.UTF_8)
                    .stream().map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("--") && !line.equals("/"))
                    .collect(Collectors.joining("\n"));
            assertOracleError(20050, () -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(preflight);
                }
            });
        }
    }

    @Test
    @Order(3)
    void uniqueForeignKeyAndCheckFailuresRollBack() throws Exception {
        String code = "IT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertDepartment(connection, code, "Y");
                assertOracleError(1, () -> insertDepartment(connection, code, "Y"));
                assertOracleError(2290, () -> insertDepartment(connection, code + "X", "X"));
                assertOracleError(2291, () -> {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO employee (employee_code, display_name, department_id, company_email, duty_summary) "
                                    + "VALUES (?, ?, ?, ?, ?)")) {
                        statement.setString(1, code);
                        statement.setString(2, "Synthetic integration employee");
                        statement.setLong(3, Long.MAX_VALUE);
                        statement.setString(4, code + "@bans.example");
                        statement.setString(5, "Synthetic integration-only duty");
                        statement.executeUpdate();
                    }
                });
            } finally {
                connection.rollback();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM department WHERE department_code = ?")) {
                statement.setString(1, code);
                try (var rows = statement.executeQuery()) {
                    rows.next();
                    assertThat(rows.getInt(1)).isZero();
                }
            }
        }
    }

    private void assertSchemaMetadata(Connection connection) throws SQLException {
        assertThat(scalar(connection, "SELECT COUNT(*) FROM user_tables WHERE table_name IN ("
                + "'DEPARTMENT','EMPLOYEE','EMPLOYEE_ACCOUNT','ACCOUNT_ACTIVATION','SYSTEM_ROLE',"
                + "'EMPLOYEE_ROLE','WORK_TASK','TASK_ASSIGNMENT','CHAT_ROOM','CHAT_MESSAGE',"
                + "'CHAT_READ_STATE','DOCUMENT','DOCUMENT_VERSION','DOCUMENT_REVIEW',"
                + "'DOCUMENT_GRANT','DOCUMENT_FRAGMENT','AUDIT_EVENT')")).isEqualTo(17);
        assertThat(scalar(connection, "SELECT COUNT(*) FROM system_role")).isEqualTo(5);
        assertThat(scalar(connection, "SELECT COUNT(*) FROM user_objects WHERE object_type='TRIGGER' "
                + "AND object_name IN ('TRG_VERSION_IMMUTABLE','TRG_FRAGMENT_IMMUTABLE','TRG_CHAT_MESSAGE_LENGTH') "
                + "AND status='VALID'")).isEqualTo(3);
    }

    private int scalar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); var rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getInt(1);
        }
    }

    private void insertDepartment(Connection connection, String code, String active) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO department (department_code, department_name, active_yn) VALUES (?, ?, ?)")) {
            statement.setString(1, code);
            statement.setString(2, "Synthetic integration department");
            statement.setString(3, active);
            statement.executeUpdate();
        }
    }

    private void assertOracleError(int expectedCode, SqlAction action) {
        assertThatThrownBy(action::run).isInstanceOf(SQLException.class)
                .satisfies(error -> assertThat(((SQLException) error).getErrorCode()).isEqualTo(expectedCode));
    }

    static List<String> statements(List<String> lines) {
        var result = new ArrayList<String>();
        var buffer = new StringBuilder();
        boolean trigger = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
            if (trimmed.startsWith("CREATE OR REPLACE TRIGGER")) trigger = true;
            if (trigger && trimmed.equals("/")) {
                result.add(buffer.toString().trim());
                buffer.setLength(0);
                trigger = false;
                continue;
            }
            buffer.append(line).append('\n');
            if (!trigger && trimmed.endsWith(";")) {
                String sql = buffer.toString().trim();
                result.add(sql.substring(0, sql.length() - 1));
                buffer.setLength(0);
            }
        }
        assertThat(buffer.toString().trim()).isEmpty();
        return result;
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws SQLException;
    }
}
