package egovframework.example.auth.service;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RolePolicyTest {
    @Test
    void employeeCannotManageAccountsAndAdminCannotGrantSelf() {
        var policy = new RolePolicy();
        var employee = actor("EMP-001", AccountStatus.ACTIVE, SecurityRole.EMPLOYEE);
        var admin = actor("EMP-003", AccountStatus.ACTIVE, SecurityRole.EMPLOYEE, SecurityRole.ACCOUNT_ADMIN);
        assertThat(policy.canManageAccounts(employee)).isFalse();
        assertThat(policy.canAssignRole(employee, "EMP-002", SecurityRole.ACCOUNT_ADMIN)).isFalse();
        assertThat(policy.canAssignRole(admin, "EMP-003", SecurityRole.ACCOUNT_ADMIN)).isFalse();
        assertThat(policy.canAssignRole(admin, "EMP-004", SecurityRole.DOCUMENT_EDITOR)).isTrue();
        assertThat(policy.canManageAccounts(actor("EMP-003", AccountStatus.SUSPENDED,
                SecurityRole.EMPLOYEE, SecurityRole.ACCOUNT_ADMIN))).isFalse();
    }

    @Test
    void anonymousAllowlistIsExact() {
        var policy = new PublicEndpointPolicy();
        assertThat(policy.allowsAnonymous("GET", "/health")).isTrue();
        assertThat(policy.allowsAnonymous("GET", "/api/health")).isTrue();
        assertThat(policy.allowsAnonymous("POST", "/api/auth/login")).isTrue();
        assertThat(policy.allowsAnonymous("GET", "/api/demo/employees")).isFalse();
        assertThat(policy.allowsAnonymous("GET", "/api/auth/login")).isFalse();
        assertThat(policy.allowsAnonymous("GET", "/api/health/anything")).isFalse();
    }

    private SessionActor actor(String id, AccountStatus status, SecurityRole... roles) {
        return new SessionActor(id, "D01", status, Set.of(roles));
    }
}
