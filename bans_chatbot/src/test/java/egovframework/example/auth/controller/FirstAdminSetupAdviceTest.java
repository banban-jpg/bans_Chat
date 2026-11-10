package egovframework.example.auth.controller;

import egovframework.example.auth.domain.AuthAccount;
import egovframework.example.auth.domain.CredentialStatus;
import egovframework.example.auth.domain.EmploymentStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.repository.AccountRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirstAdminSetupAdviceTest {
    @Test
    void onlyFreshPendingAdministratorRequiresOneTimeSetup() {
        AccountRepository accounts = mock(AccountRepository.class);
        FirstAdminSetupAdvice advice = new FirstAdminSetupAdvice(accounts);

        when(accounts.find("EMP-001")).thenReturn(Optional.empty());
        assertThat(advice.firstAdminSetupRequired()).isFalse();

        when(accounts.find("EMP-001")).thenReturn(Optional.of(admin(CredentialStatus.PENDING)));
        assertThat(advice.firstAdminSetupRequired()).isTrue();

        when(accounts.find("EMP-001")).thenReturn(Optional.of(admin(CredentialStatus.ACTIVE)));
        assertThat(advice.firstAdminSetupRequired()).isFalse();

        when(accounts.find("EMP-001")).thenReturn(Optional.of(admin(CredentialStatus.DISABLED)));
        assertThat(advice.firstAdminSetupRequired()).isFalse();
    }

    private static AuthAccount admin(CredentialStatus status) {
        return new AuthAccount(1, "EMP-001", "D01", EmploymentStatus.ACTIVE, status,
                status == CredentialStatus.PENDING ? "!PENDING_ACTIVATION!" : "<redacted>",
                status == CredentialStatus.ACTIVE ? 1 : 0, 0, null,
                Set.of(SecurityRole.EMPLOYEE, SecurityRole.ACCOUNT_ADMIN));
    }
}
