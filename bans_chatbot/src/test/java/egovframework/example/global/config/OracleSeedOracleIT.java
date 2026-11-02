package egovframework.example.global.config;

import egovframework.example.document.fixture.SyntheticDocumentFixture;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Destructive only to an explicitly opted-in empty, disposable Oracle schema; always rolls back DML. */
@Tag("oracle-integration")
@SpringBootTest
@ActiveProfiles({"oracle", "oracle-it"})
class OracleSeedOracleIT {
    @Autowired DataSource dataSource;

    @Test
    void seedIsTransactionalAndIdempotentInEmptyMigratedSchema() throws Exception {
        Assumptions.assumeTrue("YES".equals(System.getenv("ORACLE_SYNTHETIC_SEED_TEST")),
                "Set ORACLE_SYNTHETIC_SEED_TEST=YES only for a disposable migrated schema");
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                assertThat(count(connection, "SELECT COUNT(*) FROM employee")).isZero();
                assertThat(count(connection, "SELECT COUNT(*) FROM department")).isZero();
                assertThat(count(connection, "SELECT COUNT(*) FROM system_role")).isEqualTo(5);
                var lines = Files.readAllLines(Path.of("db", "oracle", "seed", "S001__synthetic_company.sql"),
                        StandardCharsets.UTF_8);
                var statements = OracleSchemaOracleIT.statements(lines);
                for (int pass = 0; pass < 2; pass++) {
                    for (String sql : statements) {
                        try (Statement statement = connection.createStatement()) { statement.execute(sql); }
                    }
                    assertThat(count(connection, "SELECT COUNT(*) FROM employee")).isEqualTo(20);
                    assertThat(count(connection, "SELECT COUNT(*) FROM employee_account")).isEqualTo(12);
                    assertThat(count(connection, "SELECT COUNT(*) FROM document")).isEqualTo(20);
                    assertThat(count(connection, "SELECT COUNT(*) FROM document_version")).isEqualTo(20);
                    assertThat(count(connection, "SELECT COUNT(*) FROM document_fragment")).isEqualTo(20);
                    assertThat(count(connection, "SELECT COUNT(*) FROM document_review")).isEqualTo(18);
                    assertThat(count(connection, "SELECT COUNT(*) FROM document_grant"))
                            .isEqualTo(SyntheticDocumentFixture.grants().size());
                    assertThat(count(connection, "SELECT COUNT(*) FROM document d LEFT JOIN document_version v "
                            + "ON v.version_id = d.current_version_id AND v.document_id = d.document_id "
                            + "WHERE v.version_id IS NULL")).isZero();
                    assertThat(count(connection, "SELECT COUNT(*) FROM document_grant g LEFT JOIN employee e "
                            + "ON e.employee_id = g.employee_id WHERE e.employee_id IS NULL")).isZero();
                }
            } finally {
                connection.rollback();
            }
            assertThat(count(connection, "SELECT COUNT(*) FROM employee")).isZero();
            assertThat(count(connection, "SELECT COUNT(*) FROM document")).isZero();
        }
    }

    private int count(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); var rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
