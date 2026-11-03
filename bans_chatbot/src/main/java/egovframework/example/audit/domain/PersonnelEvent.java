package egovframework.example.audit.domain;
/** No personnel payload, credentials or conversation text is retained. */
public record PersonnelEvent(String actor, String eventCode, String employeeId) {}
