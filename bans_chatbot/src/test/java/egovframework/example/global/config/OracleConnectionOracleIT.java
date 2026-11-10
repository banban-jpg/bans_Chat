package egovframework.example.global.config;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs only with the explicit Maven oracle-it/Failsafe profile and a local Oracle 26ai server. */
@Tag("oracle-integration")
@SpringBootTest
@ActiveProfiles({"oracle", "oracle-it"})
class OracleConnectionOracleIT {
    @Autowired DataSource dataSource;

    @Test
    void connectsToOracle26ai() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).containsIgnoringCase("Oracle");
            // 26ai is a product name; current release numbering is 23.26, not JDBC major 26.
            try (var query=connection.createStatement();var rows=query.executeQuery("SELECT banner_full FROM v$version WHERE banner_full LIKE 'Oracle%'")) {
                assertThat(rows.next()).isTrue();assertTargetBanner(rows.getString(1));
            }
        }
    }
    static void assertTargetBanner(String banner) {
        assertThat(banner).containsIgnoringCase("Oracle").containsIgnoringCase("26ai").containsIgnoringCase("Free");
    }
}
