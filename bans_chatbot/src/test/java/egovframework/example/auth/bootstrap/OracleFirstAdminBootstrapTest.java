package egovframework.example.auth.bootstrap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OracleFirstAdminBootstrapTest {
    @Test
    void absentPropertyDoesNotTouchTheDatabase() throws Exception {
        DataSource source = mock(DataSource.class);
        new OracleFirstAdminBootstrap(source, mock(PasswordEncoder.class), new MockEnvironment()).run(null);
        verifyNoInteractions(source);
    }

    @Test
    void configuredPropertyIsIgnoredAfterTheAccountWasActivated() throws Exception {
        DataSource source = sourceWithAccount("ACTIVE", "<redacted>", 1);
        PasswordEncoder passwords = mock(PasswordEncoder.class);
        var environment = new MockEnvironment().withProperty("BANS_ORACLE_FIRST_ADMIN_PASSWORD", "example-only-long-password");
        new OracleFirstAdminBootstrap(source, passwords, environment).run(null);
        verifyNoInteractions(passwords);
        verify(source, times(1)).getConnection();
    }

    @Test
    void shortPropertyFailsBeforeAnyUpdate() throws Exception {
        DataSource source = sourceWithAccount("PENDING", "!PENDING_ACTIVATION!", 0);
        PasswordEncoder passwords = mock(PasswordEncoder.class);
        var environment = new MockEnvironment().withProperty("BANS_ORACLE_FIRST_ADMIN_PASSWORD", "four");
        assertThatThrownBy(() -> new OracleFirstAdminBootstrap(source, passwords, environment).run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("ORACLE_BOOTSTRAP_PASSWORD_INVALID");
        verifyNoInteractions(passwords);
    }

    @Test
    void shortPasswordCanActivateOnlyTheOptedInLoopbackTlsMockDemo() throws Exception {
        DataSource source = sourceWithAccount("PENDING", "!PENDING_ACTIVATION!", 0);
        PasswordEncoder passwords = mock(PasswordEncoder.class);
        when(passwords.encode("four")).thenReturn("hashed");
        var environment = new MockEnvironment()
                .withProperty("BANS_ORACLE_FIRST_ADMIN_PASSWORD", "four")
                .withProperty("BANS_ORACLE_ALLOW_SHORT_DEMO_PASSWORD", "YES")
                .withProperty("server.address", "127.0.0.1")
                .withProperty("server.ssl.enabled", "true")
                .withProperty("bans.ai.provider", "mock")
                .withProperty("ORACLE_DB_USER", "BANS_CHAT");
        OracleFirstAdminBootstrap bootstrap = spy(new OracleFirstAdminBootstrap(source, passwords, environment));
        doNothing().when(bootstrap).activateOnce("hashed");

        bootstrap.run(null);

        verify(bootstrap).activateOnce("hashed");
    }

    @Test
    void freshPendingAdministratorUsesConfiguredPasswordOnce() throws Exception {
        DataSource source = sourceWithAccount("PENDING", "!PENDING_ACTIVATION!", 0);
        PasswordEncoder passwords = mock(PasswordEncoder.class);
        String localPassword = "synthetic-demo-password";
        when(passwords.encode(localPassword)).thenReturn("hashed");
        var environment = new MockEnvironment().withProperty("BANS_ORACLE_FIRST_ADMIN_PASSWORD", localPassword);
        OracleFirstAdminBootstrap bootstrap = spy(new OracleFirstAdminBootstrap(source, passwords, environment));
        doNothing().when(bootstrap).activateOnce("hashed");

        bootstrap.run(null);

        verify(passwords).encode(localPassword);
        verify(bootstrap).activateOnce("hashed");
    }

    private static DataSource sourceWithAccount(String status, String hash, long version) throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet rows = mock(ResultSet.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(rows);
        when(rows.next()).thenReturn(true, false);
        when(rows.getString(1)).thenReturn(status);
        when(rows.getString(2)).thenReturn(hash);
        when(rows.getLong(3)).thenReturn(version);
        return source;
    }
}
