package egovframework.example.global.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Locale;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/** Explicit Oracle-only boundary. No mock or embedded database fallback. */
@Configuration(proxyBeanMethods = false)
@Profile("oracle")
public class OracleDataSourceConfiguration {
    private final Environment environment;

    public OracleDataSourceConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean(destroyMethod = "close")
    public DataSource oracleDataSource() {
        String url = required("ORACLE_JDBC_URL");
        String user = required("ORACLE_DB_USER");
        String password = required("ORACLE_DB_PASSWORD");
        if (!url.startsWith("jdbc:oracle:thin:@")) {
            throw new IllegalStateException("ORACLE_CONFIG_INVALID: ORACLE_JDBC_URL must use Oracle Thin JDBC");
        }
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("ORACLE_JDBC_DRIVER_MISSING: add a verified Oracle JDBC driver");
        }

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setMaxTotal(8);
        dataSource.setMaxWait(Duration.ofSeconds(5));
        dataSource.addConnectionProperty("oracle.net.CONNECT_TIMEOUT", "5000");
        dataSource.addConnectionProperty("oracle.jdbc.ReadTimeout", "5000");
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            if (!product.toLowerCase(Locale.ROOT).contains("oracle")) {
                throw new IllegalStateException("ORACLE_PRODUCT_MISMATCH: expected Oracle Database");
            }
        } catch (SQLException exception) {
            try {
                dataSource.close();
            } catch (SQLException ignored) {
                // The startup error below intentionally omits connection details.
            }
            throw new IllegalStateException("ORACLE_26AI_UNAVAILABLE: check local Oracle 26ai and credentials");
        }
        return dataSource;
    }

    private String required(String name) {
        String value = environment.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("ORACLE_CONFIG_MISSING: " + name);
        }
        return value;
    }
}
