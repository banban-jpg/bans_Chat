package egovframework.example.document.domain;

/** Future document_grant row model; no database mapping in STEP 03. */
public record DocumentGrant(String documentId, String employeeId, DocumentPermission permission) { }
