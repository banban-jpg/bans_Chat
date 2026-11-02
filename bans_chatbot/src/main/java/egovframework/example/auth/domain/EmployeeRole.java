package egovframework.example.auth.domain;

/** Future employee_role row model; no database mapping in STEP 03. */
public record EmployeeRole(String employeeId, SecurityRole role) { }
