package egovframework.example.auth.mapper;

import java.time.OffsetDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AccountMapper {
    AccountRow find(@Param("employeeCode") String employeeCode);
    AccountRow findForUpdate(@Param("employeeCode") String employeeCode);
    List<String> activeRoles(@Param("employeeId") long employeeId);
    int activate(@Param("employeeId") long employeeId, @Param("hash") String passwordHash,
                 @Param("expectedVersion") long expectedVersion);
    int recordFailure(@Param("employeeId") long employeeId, @Param("failures") int failures,
                      @Param("lockedUntil") OffsetDateTime lockedUntil);
    int clearFailures(@Param("employeeId") long employeeId);
}
