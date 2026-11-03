package egovframework.example.auth.service;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.auth.repository.InMemoryActivationRepository;
import egovframework.example.common.exception.ApiException;
import java.time.Clock;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class ActivationConcurrencyTest {
    @Test
    void localMockAllowsOnlyOneConcurrentUseOfAnActivationCode() throws Exception {
        var encoder = new BCryptPasswordEncoder(4);
        var accounts = new InMemoryAccountRepository("", encoder, new egovframework.example.organization.repository.InMemoryOrganizationRepository());
        var activations = new InMemoryActivationRepository();
        var service = new AccountAuthenticationService(accounts, activations, encoder, Clock.systemUTC());
        accounts.provisionForTest("EMP-001", encoder.encode(UUID.randomUUID().toString()));
        var admin = new SessionActor("EMP-001", "D01", AccountStatus.ACTIVE,
                Set.of(SecurityRole.EMPLOYEE, SecurityRole.ACCOUNT_ADMIN));
        String code = service.issueActivation(admin, "EMP-002");
        var start = new CountDownLatch(1);
        Callable<Boolean> activate = () -> {
            start.await();
            try {
                service.activate("EMP-002", code, UUID.randomUUID() + "Aa1!");
                return true;
            } catch (ApiException expected) {
                return false;
            }
        };
        try (var workers = Executors.newFixedThreadPool(2)) {
            var first = workers.submit(activate);
            var second = workers.submit(activate);
            start.countDown();
            assertThat((first.get() ? 1 : 0) + (second.get() ? 1 : 0)).isEqualTo(1);
        }
        assertThat(accounts.find("EMP-002").orElseThrow().credentialsVersion()).isEqualTo(1);
    }
}
