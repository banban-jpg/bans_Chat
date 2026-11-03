package egovframework.example.auth.mapper;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.auth.service.AccountAuthenticationService;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs only through the explicit Oracle Failsafe profile against a disposable schema. */
@Tag("oracle-integration")
@SpringBootTest
@ActiveProfiles({"oracle", "oracle-it"})
class AuthOracleIT {
    @Autowired DataSource dataSource;
    @Autowired AccountMapper mapper;
    @Autowired ActivationMapper activationMapper;
    @Autowired AccountRepository accounts;
    @Autowired AccountAuthenticationService service;

    @Test
    void mapperLoadsAccountAndCredentialVersionFromMigratedSeed() {
        Assumptions.assumeTrue("YES".equals(System.getenv("ORACLE_AUTH_TEST")),
                "Use only on a dedicated migrated and seeded synthetic schema");
        assertThat(mapper.find("EMP-001")).isNotNull();
        assertThat(mapper.find("EMP-001").getAccountStatus()).isEqualTo("PENDING");
        assertThat(mapper.activeRoles(mapper.find("EMP-001").getEmployeeId()))
                .contains("EMPLOYEE", "ACCOUNT_ADMIN");
    }

    @Test
    @Transactional
    void activationConsumesHashAndChangesCredentialsAtomicallyWithRollback() {
        Assumptions.assumeTrue("YES".equals(System.getenv("ORACLE_AUTH_TEST")),
                "Use only on a dedicated migrated and seeded synthetic schema");
        var admin = mapper.findForUpdate("EMP-001");
        var target = mapper.findForUpdate("EMP-002");
        assertThat(admin.getAccountStatus()).isEqualTo("PENDING");
        assertThat(target.getAccountStatus()).isEqualTo("PENDING");
        assertThat(mapper.activate(admin.getEmployeeId(), "IT-ONLY-NOT-A-PASSWORD-HASH",
                admin.getCredentialsVersion())).isEqualTo(1);
        String code = service.issueActivation(new SessionActor("EMP-001", "D01", AccountStatus.ACTIVE,
                Set.of(SecurityRole.EMPLOYEE, SecurityRole.ACCOUNT_ADMIN)), "EMP-002");
        String password = UUID.randomUUID() + "Aa1!";
        service.activate("EMP-002", code, password);
        assertThat(accounts.find("EMP-002").orElseThrow().credentialsVersion())
                .isEqualTo(target.getCredentialsVersion() + 1);
        assertThat(service.login("EMP-002", password).eligible()).isTrue();
        // Spring test transaction rolls back the admin and target changes after this method.
    }
}
