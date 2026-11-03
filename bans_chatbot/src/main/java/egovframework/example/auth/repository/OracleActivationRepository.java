package egovframework.example.auth.repository;

import egovframework.example.auth.mapper.ActivationMapper;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("oracle")
public class OracleActivationRepository implements ActivationRepository {
    private final ActivationMapper mapper;

    public OracleActivationRepository(ActivationMapper mapper) { this.mapper = mapper; }

    @Override
    public void invalidateOutstanding(long employeeId, Instant now) {
        mapper.invalidateOutstanding(employeeId);
    }

    @Override
    public void issue(long employeeId, long issuerEmployeeId, String tokenHash, Instant expiresAt) {
        mapper.issue(employeeId, issuerEmployeeId, tokenHash, OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
    }

    @Override
    public boolean consume(long employeeId, String tokenHash, Instant now) {
        return mapper.consume(employeeId, tokenHash) == 1;
    }
}
