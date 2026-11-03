package egovframework.example.auth.domain;

import java.io.Serializable;

/** Session holds only identity and version, never a password or trusted permanent role snapshot. */
public record AuthenticatedEmployee(String employeeCode, long credentialsVersion) implements Serializable { }
