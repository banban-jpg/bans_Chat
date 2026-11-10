package egovframework.example.auth.bootstrap;

import java.io.Console;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** One-time activation of the seeded first administrator in the dedicated synthetic Oracle schema. */
@Component
@Profile("oracle & !oracle-it")
public final class OracleFirstAdminBootstrap implements ApplicationRunner {
    private final DataSource dataSource;
    private final PasswordEncoder passwords;
    private final Environment environment;

    public OracleFirstAdminBootstrap(DataSource dataSource, PasswordEncoder passwords, Environment environment) {
        this.dataSource = dataSource;
        this.passwords = passwords;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (environment.acceptsProfiles(Profiles.of("oracle-bootstrap"))) {
            runInteractive();
            return;
        }
        String configuredPassword = environment.getProperty("BANS_ORACLE_FIRST_ADMIN_PASSWORD");
        if (configuredPassword == null || configuredPassword.isEmpty() || !needsInitialActivation()) return;
        validatePassword(configuredPassword);
        activateOnce(passwords.encode(configuredPassword));
    }

    private void runInteractive() throws Exception {
        if (!"YES".equals(environment.getProperty("BANS_ORACLE_FIRST_ADMIN_BOOTSTRAP")))
            throw new IllegalStateException("ORACLE_BOOTSTRAP_NOT_ACKNOWLEDGED");
        Console console = System.console();
        if (console == null) throw new IllegalStateException("ORACLE_BOOTSTRAP_REQUIRES_INTERACTIVE_CONSOLE");

        char[] first = console.readPassword("New EMP-001 administrator password (12-128 characters): ");
        char[] second = console.readPassword("Repeat administrator password: ");
        try {
            if (first == null || second == null || first.length < 12 || first.length > 128
                    || !MessageDigest.isEqual(new String(first).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                                             new String(second).getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                throw new IllegalStateException("ORACLE_BOOTSTRAP_PASSWORD_INVALID");
            String hash = passwords.encode(new String(first));
            activateOnce(hash);
            console.printf("EMP-001 activated in the dedicated synthetic Oracle schema.%n");
        } finally {
            if (first != null) Arrays.fill(first, '\0');
            if (second != null) Arrays.fill(second, '\0');
        }
    }

    private void validatePassword(String password) {
        if (password.isBlank() || password.length() < 4 || password.length() > 128
                || (password.length() < 12 && !shortLocalDemoPasswordAllowed()))
            throw new IllegalStateException("ORACLE_BOOTSTRAP_PASSWORD_INVALID");
    }

    private boolean shortLocalDemoPasswordAllowed() {
        return "YES".equals(environment.getProperty("BANS_ORACLE_ALLOW_SHORT_DEMO_PASSWORD"))
                && "127.0.0.1".equals(environment.getProperty("server.address"))
                && "mock".equalsIgnoreCase(environment.getProperty("bans.ai.provider"))
                && "BANS_CHAT".equalsIgnoreCase(environment.getProperty("ORACLE_DB_USER"));
    }

    private boolean needsInitialActivation() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement account = connection.prepareStatement("""
                     SELECT a.account_status, a.password_hash, a.credentials_version
                     FROM employee_account a JOIN employee e ON e.employee_id = a.employee_id
                     WHERE e.employee_code = 'EMP-001'
                     """);
             ResultSet rows = account.executeQuery()) {
            if (!rows.next()) throw new IllegalStateException("ORACLE_BOOTSTRAP_ADMIN_MISSING");
            String status = rows.getString(1);
            String hash = rows.getString(2);
            long version = rows.getLong(3);
            if (rows.next()) throw new IllegalStateException("ORACLE_BOOTSTRAP_AMBIGUOUS_ADMIN");
            if (!"PENDING".equals(status)) return false;
            if (!"!PENDING_ACTIVATION!".equals(hash) || version != 0)
                throw new IllegalStateException("ORACLE_BOOTSTRAP_ALREADY_USED_OR_INVALID");
            return true;
        }
    }

    void activateOnce(String passwordHash) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                requireTarget(connection);
                long adminId;
                try (PreparedStatement lock = connection.prepareStatement("""
                        SELECT a.employee_id FROM employee_account a
                        JOIN employee e ON e.employee_id = a.employee_id
                        WHERE e.employee_code = 'EMP-001' AND e.employment_status = 'ACTIVE'
                          AND a.account_status = 'PENDING' AND a.password_hash = '!PENDING_ACTIVATION!'
                          AND a.credentials_version = 0 FOR UPDATE OF a.password_hash
                        """)) {
                    try (ResultSet rows = lock.executeQuery()) {
                        if (!rows.next()) throw new IllegalStateException("ORACLE_BOOTSTRAP_ALREADY_USED_OR_INVALID");
                        adminId = rows.getLong(1);
                        if (rows.next()) throw new IllegalStateException("ORACLE_BOOTSTRAP_AMBIGUOUS_ADMIN");
                    }
                }
                if (count(connection, "SELECT COUNT(*) FROM employee_account WHERE account_status <> 'PENDING'") != 0
                        || count(connection, "SELECT COUNT(*) FROM account_activation") != 0
                        || count(connection, "SELECT COUNT(*) FROM employee") != 20
                        || count(connection, "SELECT COUNT(*) FROM document") != 20)
                    throw new IllegalStateException("ORACLE_BOOTSTRAP_REQUIRES_FRESH_SYNTHETIC_SEED");
                try (PreparedStatement roles = connection.prepareStatement("""
                        SELECT COUNT(*) FROM employee_role
                        WHERE employee_id = ? AND role_code = 'ACCOUNT_ADMIN' AND revoked_at IS NULL
                        """)) {
                    roles.setLong(1, adminId);
                    try (ResultSet rows = roles.executeQuery()) {
                        rows.next();
                        if (rows.getInt(1) != 1) throw new IllegalStateException("ORACLE_BOOTSTRAP_ADMIN_ROLE_MISSING");
                    }
                }
                try (PreparedStatement update = connection.prepareStatement("""
                        UPDATE employee_account SET password_hash = ?, account_status = 'ACTIVE',
                            credentials_version = credentials_version + 1,
                            session_version = session_version + 1,
                            failed_login_count = 0, locked_until = NULL,
                            activated_at = SYSTIMESTAMP, updated_at = SYSTIMESTAMP
                        WHERE employee_id = ? AND account_status = 'PENDING'
                          AND password_hash = '!PENDING_ACTIVATION!' AND credentials_version = 0
                        """)) {
                    update.setString(1, passwordHash);
                    update.setLong(2, adminId);
                    if (update.executeUpdate() != 1) throw new IllegalStateException("ORACLE_BOOTSTRAP_UPDATE_CONFLICT");
                }
                try (PreparedStatement audit = connection.prepareStatement("""
                        INSERT INTO audit_event (actor_employee_id, event_code, resource_type, resource_id, outcome_code)
                        VALUES (?, 'FIRST_ADMIN_BOOTSTRAP', 'EMPLOYEE_ACCOUNT', 'EMP-001', 'SUCCESS')
                        """)) {
                    audit.setLong(1, adminId);
                    audit.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException failure) {
                connection.rollback();
                throw failure;
            }
        }
    }

    private void requireTarget(Connection connection) throws SQLException {
        if (!connection.getMetaData().getDatabaseProductName().toLowerCase().contains("oracle"))
            throw new IllegalStateException("ORACLE_BOOTSTRAP_TARGET_MISMATCH");
        try (PreparedStatement target = connection.prepareStatement("""
                SELECT USER, SYS_CONTEXT('USERENV','CON_NAME') FROM dual
                """); ResultSet rows = target.executeQuery()) {
            rows.next();
            if (!rows.getString(1).equalsIgnoreCase("BANS_CHAT")
                    || !rows.getString(1).equalsIgnoreCase(environment.getRequiredProperty("ORACLE_DB_USER"))
                    || !rows.getString(2).equalsIgnoreCase(environment.getProperty("BANS_ORACLE_BOOTSTRAP_PDB", "FREEPDB1")))
                throw new IllegalStateException("ORACLE_BOOTSTRAP_TARGET_MISMATCH");
        }
        if (count(connection, "SELECT COUNT(*) FROM user_tables") != 18
                || count(connection, "SELECT COUNT(*) FROM system_role") != 5)
            throw new IllegalStateException("ORACLE_BOOTSTRAP_SCHEMA_MISMATCH");
    }

    private static int count(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
