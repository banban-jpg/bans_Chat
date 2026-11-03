package egovframework.example.auth.mapper;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** Oracle row carrier; intentionally has no generated toString containing the password hash. */
@Getter
@Setter
public class AccountRow {
    private long employeeId;
    private String employeeCode;
    private String departmentCode;
    private String employmentStatus;
    private String accountStatus;
    private String passwordHash;
    private long credentialsVersion;
    private int failedLoginCount;
    private OffsetDateTime lockedUntil;
}
