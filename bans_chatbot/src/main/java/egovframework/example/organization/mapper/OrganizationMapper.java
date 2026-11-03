package egovframework.example.organization.mapper;
import egovframework.example.organization.dto.DirectoryModels.*;
import egovframework.example.organization.domain.Department;
import java.util.List;
import org.apache.ibatis.annotations.Param;
public interface OrganizationMapper {
 List<EmployeeView> search(Search q);
 long count(Search q);
 List<Department> departments();
 State state(@Param("id") String id);
 String lockEmployee(@Param("id") String id);
 int create(Create e);
 int change(@Param("id") String id,@Param("status") String status,@Param("department") String department);
}
