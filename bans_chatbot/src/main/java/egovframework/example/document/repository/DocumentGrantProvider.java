package egovframework.example.document.repository;

import egovframework.example.document.domain.DocumentPermission;

/** Resource grant lookup; Oracle implementation arrives with the schema in STEP 04. */
public interface DocumentGrantProvider {
    boolean hasGrant(String documentId, String employeeId, DocumentPermission permission);
}
