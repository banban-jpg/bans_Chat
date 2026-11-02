package egovframework.example.auth.service;

import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import org.springframework.stereotype.Component;

@Component
public class RolePolicy {
    public boolean canManageAccounts(SessionActor actor) {
        return actor != null && actor.has(SecurityRole.ACCOUNT_ADMIN);
    }

    public boolean canAssignRole(SessionActor actor, String targetEmployeeId, SecurityRole role) {
        return canManageAccounts(actor) && role != null && targetEmployeeId != null
                && !targetEmployeeId.isBlank() && !actor.employeeId().equals(targetEmployeeId);
    }

    public boolean canAudit(SessionActor actor) {
        return actor != null && actor.has(SecurityRole.AUDITOR);
    }
}
