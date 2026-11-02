package egovframework.example.organization.fixture;

import egovframework.example.common.fixture.FixtureRows;
import egovframework.example.organization.domain.Department;
import egovframework.example.organization.domain.Employee;
import java.util.List;

/** Invented company only. These records are not a persistent employee directory. */
public final class SyntheticOrganizationFixture {
    private static final List<Department> DEPARTMENTS = FixtureRows.read("departments.psv", 2).stream()
            .map(row -> new Department(row[0], row[1])).toList();
    private static final List<Employee> EMPLOYEES = FixtureRows.read("employees.psv", 7).stream()
            .map(row -> new Employee(row[0], row[1], row[2], row[3], row[4], row[5], row[6])).toList();

    private SyntheticOrganizationFixture() { }

    public static List<Department> departments() { return DEPARTMENTS; }
    public static List<Employee> employees() { return EMPLOYEES; }
}
