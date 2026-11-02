package egovframework.example.organization.domain;

public record Employee(String employeeId, String displayName, String departmentCode,
                       String jobTitle, String extensionNo, String companyEmail, String dutySummary) { }
