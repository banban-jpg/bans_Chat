package egovframework.example.auth.repository;

import egovframework.example.auth.domain.AuthAccount;
import egovframework.example.auth.domain.CredentialStatus;
import egovframework.example.auth.domain.EmploymentStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.mapper.AccountMapper;
import egovframework.example.auth.mapper.AccountRow;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("oracle")
public class OracleAccountRepository implements AccountRepository {
    private final AccountMapper mapper;

    public OracleAccountRepository(AccountMapper mapper) { this.mapper = mapper; }

    @Override
    public Optional<AuthAccount> find(String employeeCode) {
        return Optional.ofNullable(convert(mapper.find(employeeCode)));
    }

    @Override
    @Transactional
    public <T> T withLockedAccount(String employeeCode, Function<AuthAccount, T> action) {
        return action.apply(convert(mapper.findForUpdate(employeeCode)));
    }

    @Override
    public boolean activate(long employeeId, String passwordHash, long expectedVersion) {
        return mapper.activate(employeeId, passwordHash, expectedVersion) == 1;
    }

    @Override
    public void recordFailedLogin(long employeeId, int failures, Instant lockedUntil) {
        mapper.recordFailure(employeeId, failures, utc(lockedUntil));
    }

    @Override
    public void clearFailedLogins(long employeeId) { mapper.clearFailures(employeeId); }

    private AuthAccount convert(AccountRow row) {
        if (row == null) return null;
        var roles = mapper.activeRoles(row.getEmployeeId()).stream()
                .map(SecurityRole::valueOf).collect(Collectors.toUnmodifiableSet());
        return new AuthAccount(row.getEmployeeId(), row.getEmployeeCode(), row.getDepartmentCode(),
                EmploymentStatus.valueOf(row.getEmploymentStatus()),
                CredentialStatus.valueOf(row.getAccountStatus()), row.getPasswordHash(),
                row.getCredentialsVersion(), row.getFailedLoginCount(),
                row.getLockedUntil() == null ? null : row.getLockedUntil().toInstant(), roles);
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
