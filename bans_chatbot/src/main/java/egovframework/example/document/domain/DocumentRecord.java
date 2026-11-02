package egovframework.example.document.domain;

import java.util.Objects;

/** Trusted metadata loaded from the document store, not from request JSON. */
public record DocumentRecord(String documentId, String ownerEmployeeId, String departmentCode,
                             DocumentScope scope, boolean approved) {
    public DocumentRecord {
        Objects.requireNonNull(documentId);
        Objects.requireNonNull(ownerEmployeeId);
        Objects.requireNonNull(scope);
        if (scope == DocumentScope.DEPARTMENT && (departmentCode == null || departmentCode.isBlank())) {
            throw new IllegalArgumentException("Department document requires a department");
        }
    }
}
