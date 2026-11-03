package egovframework.example.auth.repository;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"local-mock", "test"})
public class InMemoryActivationRepository implements ActivationRepository {
    private final Map<Long, Activation> current = new ConcurrentHashMap<>();

    public void resetForTest() { current.clear(); }

    @Override
    public void invalidateOutstanding(long employeeId, Instant now) {
        current.remove(employeeId);
    }

    @Override
    public void issue(long employeeId, long issuerEmployeeId, String tokenHash, Instant expiresAt) {
        current.put(employeeId, new Activation(tokenHash, expiresAt));
    }

    @Override
    public boolean consume(long employeeId, String tokenHash, Instant now) {
        Activation activation = current.get(employeeId);
        return activation != null && activation.tokenHash.equals(tokenHash)
                && now.isBefore(activation.expiresAt) && current.remove(employeeId, activation);
    }

    private record Activation(String tokenHash, Instant expiresAt) { }
}
