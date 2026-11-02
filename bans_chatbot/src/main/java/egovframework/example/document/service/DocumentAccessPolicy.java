package egovframework.example.document.service;

import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.common.exception.ApiException;
import egovframework.example.document.domain.DocumentPermission;
import egovframework.example.document.domain.DocumentRecord;
import egovframework.example.document.domain.DocumentScope;
import egovframework.example.document.repository.DocumentGrantProvider;
import org.springframework.http.HttpStatus;

/** Evaluates trusted session identity and resource metadata on every operation. */
public class DocumentAccessPolicy {
    private final DocumentGrantProvider grants;

    public DocumentAccessPolicy(DocumentGrantProvider grants) {
        this.grants = grants;
    }

    public boolean canRead(SessionActor actor, DocumentRecord document) {
        if (actor == null || !actor.activeEmployee() || document == null) return false;
        String employeeId = actor.employeeId();
        if (employeeId.equals(document.ownerEmployeeId())) return true;
        if (grants.hasGrant(document.documentId(), employeeId, DocumentPermission.READ)) return true;
        if (!document.approved()) return false;
        if (document.scope() == DocumentScope.COMPANY) return true;
        return document.scope() == DocumentScope.DEPARTMENT
                && actor.departmentCode().equals(document.departmentCode());
    }

    /** Search results must be filtered by exactly the same decision as direct reads. */
    public boolean canAppearInSearch(SessionActor actor, DocumentRecord document) {
        return canRead(actor, document);
    }

    public void requireRead(SessionActor actor, DocumentRecord document) {
        if (actor == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Authentication required.");
        if (!canRead(actor, document)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found.");
        }
    }

    public boolean canApprove(SessionActor actor, DocumentRecord document) {
        return actor != null && actor.has(SecurityRole.DOCUMENT_MANAGER) && canRead(actor, document)
                && grants.hasGrant(document.documentId(), actor.employeeId(), DocumentPermission.APPROVE);
    }
}
