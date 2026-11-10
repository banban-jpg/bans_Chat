package egovframework.example.organization.service;

import egovframework.example.organization.domain.Employee;
import egovframework.example.organization.repository.EmployeeDirectory;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"test"})
public class EmployeeDirectoryService {
    private final EmployeeDirectory directory;

    public EmployeeDirectoryService(EmployeeDirectory directory) {
        this.directory = directory;
    }

    public List<Employee> syntheticEmployees() {
        return directory.findAll();
    }
}
