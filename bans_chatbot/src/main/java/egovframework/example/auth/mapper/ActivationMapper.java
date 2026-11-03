package egovframework.example.auth.mapper;

import java.time.OffsetDateTime;
import org.apache.ibatis.annotations.Param;

public interface ActivationMapper {
    int invalidateOutstanding(@Param("employeeId") long employeeId);
    int issue(@Param("employeeId") long employeeId, @Param("issuerEmployeeId") long issuerEmployeeId,
              @Param("tokenHash") String tokenHash, @Param("expiresAt") OffsetDateTime expiresAt);
    int consume(@Param("employeeId") long employeeId, @Param("tokenHash") String tokenHash);
}
