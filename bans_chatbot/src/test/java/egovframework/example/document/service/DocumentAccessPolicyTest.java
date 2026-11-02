package egovframework.example.document.service;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.common.exception.ApiException;
import egovframework.example.document.domain.DocumentPermission;
import egovframework.example.document.domain.DocumentRecord;
import egovframework.example.document.domain.DocumentScope;
import egovframework.example.document.repository.DocumentGrantProvider;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentAccessPolicyTest {
    private final DocumentGrantProvider grants = mock(DocumentGrantProvider.class);
    private final DocumentAccessPolicy policy = new DocumentAccessPolicy(grants);

    @Test
    void crossDepartmentAndPrivateDocumentsNeedGrantEvenForAdmin() {
        var other = actor("EMP-003", "D02", AccountStatus.ACTIVE, SecurityRole.ACCOUNT_ADMIN);
        var department = doc(DocumentScope.DEPARTMENT, true);
        var privateDoc = doc(DocumentScope.PRIVATE, true);
        assertThat(policy.canRead(other, department)).isFalse();
        assertThat(policy.canAppearInSearch(other, department)).isFalse();
        assertThat(policy.canRead(other, privateDoc)).isFalse();
        when(grants.hasGrant("DOC-1", "EMP-003", DocumentPermission.READ)).thenReturn(true);
        assertThat(policy.canRead(other, privateDoc)).isTrue();
        assertThat(policy.canAppearInSearch(other, privateDoc)).isTrue();
    }

    @Test
    void deniedReadHidesDocumentExistence() {
        var other = actor("EMP-003", "D02", AccountStatus.ACTIVE);
        var privateDoc = doc(DocumentScope.PRIVATE, true);
        assertThatThrownBy(() -> policy.requireRead(other, privateDoc))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).code()).isEqualTo("NOT_FOUND"));
        assertThatThrownBy(() -> policy.requireRead(null, privateDoc))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).code()).isEqualTo("AUTH_REQUIRED"));
    }

    @Test
    void inactiveAccountsCannotReadEvenOwnDocument() {
        var privateDoc = doc(DocumentScope.PRIVATE, true);
        assertThat(policy.canRead(actor("EMP-001", "D01", AccountStatus.SUSPENDED), privateDoc)).isFalse();
        assertThat(policy.canRead(actor("EMP-001", "D01", AccountStatus.DEPARTED), privateDoc)).isFalse();
    }

    @Test
    void onlyExplicitlyAssignedManagerCanApprove() {
        var draft = doc(DocumentScope.PRIVATE, false);
        var editor = actor("EMP-002", "D01", AccountStatus.ACTIVE, SecurityRole.DOCUMENT_EDITOR);
        var manager = actor("EMP-003", "D02", AccountStatus.ACTIVE, SecurityRole.DOCUMENT_MANAGER);
        when(grants.hasGrant("DOC-1", "EMP-002", DocumentPermission.READ)).thenReturn(true);
        when(grants.hasGrant("DOC-1", "EMP-002", DocumentPermission.APPROVE)).thenReturn(true);
        assertThat(policy.canApprove(editor, draft)).isFalse();
        assertThat(policy.canApprove(manager, draft)).isFalse();
        when(grants.hasGrant("DOC-1", "EMP-003", DocumentPermission.APPROVE)).thenReturn(true);
        assertThat(policy.canApprove(manager, draft)).isFalse();
        when(grants.hasGrant("DOC-1", "EMP-003", DocumentPermission.READ)).thenReturn(true);
        assertThat(policy.canApprove(manager, draft)).isTrue();
    }

    private SessionActor actor(String id, String dept, AccountStatus status, SecurityRole... extra) {
        var roles = new HashSet<>(Set.of(SecurityRole.EMPLOYEE));
        roles.addAll(Set.of(extra));
        return new SessionActor(id, dept, status, roles);
    }

    private DocumentRecord doc(DocumentScope scope, boolean approved) {
        return new DocumentRecord("DOC-1", "EMP-001", "D01", scope, approved);
    }
}
