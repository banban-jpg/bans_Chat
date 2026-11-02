package egovframework.example.organization.fixture;

import egovframework.example.organization.domain.Employee;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyntheticOrganizationFixtureTest {
    @Test
    void companyHasExactlyTwentyEmployeesInFiveExpectedDepartments() {
        var departments = SyntheticOrganizationFixture.departments();
        var employees = SyntheticOrganizationFixture.employees();
        assertThat(departments).hasSize(5);
        assertThat(employees).hasSize(20);
        assertThat(departments.stream().map(d -> d.departmentCode()).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder("D01", "D02", "D03", "D04", "D05");
        assertThat(employees.stream().collect(Collectors.groupingBy(Employee::departmentCode,
                Collectors.counting()))).isEqualTo(Map.of("D01", 4L, "D02", 3L,
                "D03", 6L, "D04", 4L, "D05", 3L));
        assertThat(employees.stream().map(Employee::employeeId).collect(Collectors.toSet())).hasSize(20);
        assertThat(employees.stream().map(Employee::extensionNo).collect(Collectors.toSet())).hasSize(20);
        assertThat(employees.stream().map(Employee::companyEmail).collect(Collectors.toSet())).hasSize(20);
        assertThat(employees).allSatisfy(e -> {
            assertThat(e.companyEmail()).matches("emp\\d{3}@bans\\.example");
            assertThat(e.jobTitle()).isNotBlank();
            assertThat(e.dutySummary()).isNotBlank();
            assertThat(Set.of("D01", "D02", "D03", "D04", "D05")).contains(e.departmentCode());
        });
    }
}
