package egovframework.example.organization.repository;

import egovframework.example.organization.domain.Employee;
import egovframework.example.organization.fixture.SyntheticOrganizationFixture;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Synthetic fixture only; never a persistent repository. */
@Repository
@Profile({"test"})
public class InMemoryEmployeeDirectory implements EmployeeDirectory {
    private final List<Employee> employees = SyntheticOrganizationFixture.employees();

    @Override
    public List<Employee> findAll() {
        return employees;
    }
}
