package egovframework.example.organization.repository;
import egovframework.example.organization.dto.DirectoryModels.*;
import egovframework.example.organization.domain.Department;
import java.util.List;
public interface OrganizationRepository {
 Page search(Search search);
 List<Department> departments();
 void create(Create employee, String actor);
 void change(String employeeId, String status, String departmentId, String actor);
 State state(String employeeId);
}
