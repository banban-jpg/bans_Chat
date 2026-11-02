package egovframework.example.auth.domain;

import java.util.Objects;
import java.util.Set;

/** Trusted server-side identity snapshot, never constructed from a request userId. */
public record SessionActor(String employeeId, String departmentCode, AccountStatus status,
                           Set<SecurityRole> roles) {
    public SessionActor {
        Objects.requireNonNull(employeeId);
        Objects.requireNonNull(departmentCode);
        Objects.requireNonNull(status);
        roles = Set.copyOf(Objects.requireNonNull(roles));
        if (employeeId.isBlank() || departmentCode.isBlank()) {
            throw new IllegalArgumentException("identity fields must be present");
        }
    }

    public boolean activeEmployee() {
        return status == AccountStatus.ACTIVE && roles.contains(SecurityRole.EMPLOYEE);
    }

    public boolean has(SecurityRole role) {
        return activeEmployee() && roles.contains(role);
    }
}
