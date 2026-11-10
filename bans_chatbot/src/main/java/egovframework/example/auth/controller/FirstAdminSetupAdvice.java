package egovframework.example.auth.controller;

import egovframework.example.auth.domain.CredentialStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.common.web.WorkbenchController;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** The public shell exposes only whether the seeded first administrator needs setup. */
@ControllerAdvice(assignableTypes = WorkbenchController.class)
@Profile("oracle")
public final class FirstAdminSetupAdvice {
    private final AccountRepository accounts;

    public FirstAdminSetupAdvice(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @ModelAttribute("firstAdminSetupRequired")
    public boolean firstAdminSetupRequired() {
        return accounts.find("EMP-001")
                .map(account -> account.credentialStatus() == CredentialStatus.PENDING
                        && account.credentialsVersion() == 0
                        && "!PENDING_ACTIVATION!".equals(account.passwordHash())
                        && account.roles().contains(SecurityRole.ACCOUNT_ADMIN))
                .orElse(false);
    }
}
