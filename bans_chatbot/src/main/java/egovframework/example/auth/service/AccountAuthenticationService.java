package egovframework.example.auth.service;

import egovframework.example.auth.domain.AuthAccount;
import egovframework.example.auth.domain.CredentialStatus;
import egovframework.example.auth.domain.EmploymentStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.auth.repository.ActivationRepository;
import egovframework.example.common.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Authentication decisions use locked, fresh account rows; no password or code is logged. */
@Service
public class AccountAuthenticationService {
    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final Duration CODE_LIFETIME = Duration.ofHours(24);
    private final AccountRepository accounts;
    private final ActivationRepository activations;
    private final PasswordEncoder passwords;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private final String dummyHash;

    public AccountAuthenticationService(AccountRepository accounts, ActivationRepository activations,
                                        PasswordEncoder passwords, Clock clock) {
        this.accounts = accounts;
        this.activations = activations;
        this.passwords = passwords;
        this.clock = clock;
        byte[] dummy = new byte[32];
        random.nextBytes(dummy);
        dummyHash = passwords.encode(Base64.getEncoder().encodeToString(dummy));
    }

    /** Internal administrator operation. The caller must deliver the returned code out of band. */
    public String issueActivation(SessionActor administrator, String targetEmployeeCode) {
        if (administrator == null || !administrator.has(SecurityRole.ACCOUNT_ADMIN)) throw forbidden();
        AuthAccount issuer = accounts.find(administrator.employeeId()).orElseThrow(this::forbidden);
        if (!issuer.eligible() || !issuer.roles().contains(SecurityRole.ACCOUNT_ADMIN)
                || issuer.employeeCode().equals(targetEmployeeCode)) throw forbidden();
        return accounts.withLockedAccount(targetEmployeeCode, target -> {
            if (target == null || target.credentialStatus() != CredentialStatus.PENDING
                    || target.employmentStatus() != EmploymentStatus.ACTIVE) throw conflict();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            var now = clock.instant();
            activations.invalidateOutstanding(target.employeeId(), now);
            activations.issue(target.employeeId(), issuer.employeeId(), hash(code), now.plus(CODE_LIFETIME));
            return code;
        });
    }

    public void activate(String employeeCode, String code, String newPassword) {
        if (code == null || code.isBlank()) throw conflict();
        validatePassword(newPassword);
        accounts.withLockedAccount(employeeCode, account -> {
            if (account == null || account.credentialStatus() != CredentialStatus.PENDING
                    || account.employmentStatus() != EmploymentStatus.ACTIVE) throw conflict();
            String encoded = passwords.encode(newPassword);
            if (!activations.consume(account.employeeId(), hash(code), clock.instant())) throw conflict();
            if (!accounts.activate(account.employeeId(), encoded, account.credentialsVersion())) throw conflict();
            return null;
        });
    }

    public AuthAccount login(String employeeCode, String password) {
        if (employeeCode == null || employeeCode.isBlank() || password == null) throw unauthorized();
        LoginOutcome outcome = accounts.withLockedAccount(employeeCode, account -> {
            if (account == null) {
                passwords.matches(password, dummyHash);
                return new LoginOutcome(null, false);
            }
            var now = clock.instant();
            if (account.lockedUntil() != null && now.isBefore(account.lockedUntil())) {
                return new LoginOutcome(null, true);
            }
            boolean eligible = account.eligible();
            boolean passwordMatches = passwords.matches(password, eligible ? account.passwordHash() : dummyHash);
            boolean valid = eligible && passwordMatches;
            if (!valid) {
                int failures = account.lockedUntil() != null ? 1 : account.failedLoginCount() + 1;
                accounts.recordFailedLogin(account.employeeId(), failures,
                        failures >= MAX_FAILURES ? now.plus(LOCK_DURATION) : null);
                return new LoginOutcome(null, failures >= MAX_FAILURES);
            }
            accounts.clearFailedLogins(account.employeeId());
            return new LoginOutcome(account, false);
        });
        if (outcome.rateLimited()) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests.");
        }
        if (outcome.account() == null) throw unauthorized();
        return outcome.account();
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12 || password.length() > 128 || password.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Invalid request.");
        }
    }

    private static String hash(String code) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable");
        }
    }

    private ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Authentication required.");
    }

    private ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied.");
    }

    private ApiException conflict() {
        return new ApiException(HttpStatus.CONFLICT, "ACTIVATION_CONFLICT", "Activation unavailable.");
    }

    private record LoginOutcome(AuthAccount account, boolean rateLimited) { }
}
