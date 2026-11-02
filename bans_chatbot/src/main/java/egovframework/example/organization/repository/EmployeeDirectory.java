package egovframework.example.organization.repository;

import egovframework.example.organization.domain.Employee;
import java.util.List;

public interface EmployeeDirectory {
    List<Employee> findAll();
}
