package egovframework.example.organization.service;

import egovframework.example.organization.domain.Employee;
import egovframework.example.organization.repository.EmployeeDirectory;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmployeeDirectoryServiceTest {
    @Test
    void serviceReadsOnlyThroughItsDomainPort() {
        EmployeeDirectory directory = mock(EmployeeDirectory.class);
        List<Employee> fixture = List.of(new Employee("EMP-001", "가상직원01", "D01"));
        when(directory.findAll()).thenReturn(fixture);

        assertThat(new EmployeeDirectoryService(directory).syntheticEmployees()).isEqualTo(fixture);
        verify(directory).findAll();
    }
}
