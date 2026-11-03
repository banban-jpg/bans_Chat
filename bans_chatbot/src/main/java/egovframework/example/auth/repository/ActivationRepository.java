package egovframework.example.auth.repository;

import java.time.Instant;

public interface ActivationRepository {
    void invalidateOutstanding(long employeeId, Instant now);
    void issue(long employeeId, long issuerEmployeeId, String tokenHash, Instant expiresAt);
    boolean consume(long employeeId, String tokenHash, Instant now);
}
