package egovframework.example.global.config;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Source-only check. This does not validate Oracle syntax or execute DML. */
class OracleSeedSourceTest {
    private static final Path DIR = Path.of("db", "oracle", "seed");

    @Test
    void checkedInOracleSqlMatchesCanonicalFixturesExactly() throws Exception {
        String committed = Files.readString(DIR.resolve("S001__synthetic_company.sql"), StandardCharsets.UTF_8);
        assertThat(committed).isEqualTo(SyntheticOracleSeedRenderer.render());
        assertThat(committed).contains("MERGE INTO department", "MERGE INTO employee_account",
                "MERGE INTO employee_role", "MERGE INTO document_fragment",
                "UPDATE document_version", "UPDATE document");
        assertThat(committed).doesNotContainPattern("(?im)^\\s*(DROP|TRUNCATE|DELETE|COMMIT|ROLLBACK)\\b");
        assertThat(committed).doesNotContain("account_activation", "api_key", "gemini_key");
        assertThat(committed).contains("'!PENDING_ACTIVATION!', 'PENDING'");
    }

    @Test
    void explicitRunnerGuardsSchemaAndRollsBackOnError() throws Exception {
        String runner = Files.readString(DIR.resolve("apply_seed.sql"), StandardCharsets.UTF_8);
        String preflight = Files.readString(DIR.resolve("verify_seed_preconditions.sql"), StandardCharsets.UTF_8);
        String verify = Files.readString(DIR.resolve("verify_seed.sql"), StandardCharsets.UTF_8);
        assertThat(runner).contains("WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK",
                "@@verify_seed_preconditions.sql", "@@S001__synthetic_company.sql",
                "@@verify_seed.sql", "COMMIT;");
        assertThat(preflight).contains("SYNTHETIC_SEED_REQUIRES_V001_V002_SCHEMA",
                "SYNTHETIC_SEED_REQUIRES_PENDING_ACCOUNTS_ONLY");
        assertThat(verify).contains("SEED_EMPLOYEE_COUNT", "SEED_DOCUMENT_COUNT",
                "SEED_PENDING_ACCOUNT_COUNT", "SEED_EXTERNAL_SEGMENT_COUNT");
    }
}
