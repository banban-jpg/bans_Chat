package egovframework.example.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.Set;

/** Fresh server-side account view. The password hash is never serialized to an API response. */
public record AuthAccount(long employeeId, String employeeCode, String departmentCode,
                          EmploymentStatus employmentStatus, CredentialStatus credentialStatus,
                          String passwordHash, long credentialsVersion, int failedLoginCount,
                          Instant lockedUntil, Set<SecurityRole> roles) {
    public AuthAccount {
        roles = Set.copyOf(roles);
    }

    @Override
    @JsonIgnore
    public String passwordHash() {
        return passwordHash;
    }

    @Override
    public String toString() {
        return "AuthAccount[employeeCode=" + employeeCode + ", passwordHash=<redacted>]";
    }

    public boolean eligible() {
        return employmentStatus == EmploymentStatus.ACTIVE
                && credentialStatus == CredentialStatus.ACTIVE && roles.contains(SecurityRole.EMPLOYEE);
    }

    public SessionActor actor() {
        return new SessionActor(employeeCode, departmentCode, AccountStatus.ACTIVE, roles);
    }
}
