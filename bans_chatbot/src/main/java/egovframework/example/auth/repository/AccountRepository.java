package egovframework.example.auth.repository;

import egovframework.example.auth.domain.AuthAccount;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

/** withLockedAccount keeps one account locked through its callback. Oracle uses a transaction and FOR UPDATE. */
public interface AccountRepository {
    Optional<AuthAccount> find(String employeeCode);
    <T> T withLockedAccount(String employeeCode, Function<AuthAccount, T> action);
    boolean activate(long employeeId, String passwordHash, long expectedVersion);
    void recordFailedLogin(long employeeId, int failures, Instant lockedUntil);
    void clearFailedLogins(long employeeId);
}
