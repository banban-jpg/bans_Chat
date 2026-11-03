package egovframework.example.auth.repository;

import egovframework.example.auth.domain.AuthAccount;
import egovframework.example.auth.domain.CredentialStatus;
import egovframework.example.auth.domain.EmploymentStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.fixture.SyntheticAccountFixture;
import egovframework.example.organization.fixture.SyntheticOrganizationFixture;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

/** Process-local, thread-safe fixture store. Restart loses all activation and login changes. */
@Repository
@Profile({"local-mock", "test"})
public class InMemoryAccountRepository implements AccountRepository {
    private final egovframework.example.organization.repository.OrganizationRepository organization;

    private final Map<String, Mutable> byCode = new ConcurrentHashMap<>();
    private final Map<Long, Mutable> byId = new ConcurrentHashMap<>();

    public InMemoryAccountRepository(@Value("${BANS_LOCAL_ADMIN_PASSWORD:}") String bootstrapPassword,
                                     PasswordEncoder encoder, egovframework.example.organization.repository.OrganizationRepository organization) {
        this.organization = organization;
        loadFixtures();
        if (!bootstrapPassword.isBlank()) {
            if (bootstrapPassword.length() < 12 || bootstrapPassword.length() > 128) {
                throw new IllegalStateException("BANS_LOCAL_ADMIN_PASSWORD must be 12-128 characters");
            }
            provisionForTest("EMP-001", encoder.encode(bootstrapPassword));
        }
    }

    public synchronized void resetForTest() {
        byCode.clear();
        byId.clear();
        loadFixtures();
    }

    private void loadFixtures() {
        var departments = SyntheticOrganizationFixture.employees().stream()
                .collect(java.util.stream.Collectors.toMap(e -> e.employeeId(), e -> e.departmentCode()));
        for (var fixture : SyntheticAccountFixture.accounts()) {
            long id = Long.parseLong(fixture.employeeCode().substring(4));
            var row = new Mutable(id, fixture.employeeCode(), departments.get(fixture.employeeCode()), fixture.roles());
            byCode.put(fixture.employeeCode(), row);
            byId.put(id, row);
        }
    }

    @Override
    public Optional<AuthAccount> find(String employeeCode) {
        Mutable row = byCode.get(employeeCode);
        if (row == null) return Optional.empty();
        synchronized (row) {
            var state = organization.state(employeeCode);
            var a = row.snapshot();
            return Optional.of(state == null ? a : new AuthAccount(a.employeeId(), a.employeeCode(), state.departmentCode(),
                EmploymentStatus.valueOf(state.status()), a.credentialStatus(), a.passwordHash(), a.credentialsVersion(),
                a.failedLoginCount(), a.lockedUntil(), a.roles()));
        }
    }

    @Override
    public <T> T withLockedAccount(String employeeCode, Function<AuthAccount, T> action) {
        Mutable row = byCode.get(employeeCode);
        if (row == null) return action.apply(null);
        synchronized (row) { return action.apply(find(employeeCode).orElse(null)); }
    }

    @Override
    public boolean activate(long employeeId, String passwordHash, long expectedVersion) {
        Mutable row = byId.get(employeeId);
        if (row == null) return false;
        synchronized (row) {
            if (row.credentialStatus != CredentialStatus.PENDING || row.credentialsVersion != expectedVersion) return false;
            row.passwordHash = passwordHash;
            row.credentialStatus = CredentialStatus.ACTIVE;
            row.credentialsVersion++;
            row.failedLoginCount = 0;
            row.lockedUntil = null;
            return true;
        }
    }

    @Override
    public void recordFailedLogin(long employeeId, int failures, Instant lockedUntil) {
        Mutable row = byId.get(employeeId);
        if (row == null) return;
        synchronized (row) { row.failedLoginCount = failures; row.lockedUntil = lockedUntil; }
    }

    @Override
    public void clearFailedLogins(long employeeId) {
        Mutable row = byId.get(employeeId);
        if (row == null) return;
        synchronized (row) { row.failedLoginCount = 0; row.lockedUntil = null; }
    }

    /** Tests create a random password at runtime; no usable default credential is checked in. */
    public void provisionForTest(String employeeCode, String encodedPassword) {
        Mutable row = byCode.get(employeeCode);
        if (row == null) throw new IllegalArgumentException("Unknown synthetic employee");
        synchronized (row) {
            row.passwordHash = encodedPassword;
            row.credentialStatus = CredentialStatus.ACTIVE;
            row.credentialsVersion++;
            row.failedLoginCount = 0;
            row.lockedUntil = null;
        }
    }

    public void setStatusForTest(String employeeCode, CredentialStatus status) {
        Mutable row = byCode.get(employeeCode);
        synchronized (row) { row.credentialStatus = status; row.credentialsVersion++; }
    }

    private static final class Mutable {
        final long id;
        final String code;
        final String department;
        final Set<SecurityRole> roles;
        EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;
        CredentialStatus credentialStatus = CredentialStatus.PENDING;
        String passwordHash = "!PENDING_ACTIVATION!";
        long credentialsVersion;
        int failedLoginCount;
        Instant lockedUntil;

        Mutable(long id, String code, String department, Set<SecurityRole> roles) {
            this.id = id; this.code = code; this.department = department; this.roles = Set.copyOf(roles);
        }

        AuthAccount snapshot() {
            return new AuthAccount(id, code, department, employmentStatus, credentialStatus,
                    passwordHash, credentialsVersion, failedLoginCount, lockedUntil, roles);
        }
    }
}
