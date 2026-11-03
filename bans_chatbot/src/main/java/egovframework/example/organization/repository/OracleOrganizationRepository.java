package egovframework.example.organization.repository;
import egovframework.example.organization.dto.DirectoryModels.*;
import egovframework.example.organization.domain.Department;
import egovframework.example.organization.mapper.OrganizationMapper;
import egovframework.example.audit.mapper.PersonnelAuditMapper;
import egovframework.example.audit.domain.PersonnelEvent;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Repository @Profile("oracle")
public class OracleOrganizationRepository implements OrganizationRepository {
 private final OrganizationMapper mapper;
 private final PersonnelAuditMapper audit;
 public OracleOrganizationRepository(OrganizationMapper mapper,PersonnelAuditMapper audit) { this.mapper=mapper; this.audit=audit; }
 private void record(PersonnelEvent event) { if(audit.insert(event)!=1) throw new IllegalStateException("PERSONNEL_AUDIT_REQUIRED"); }
 public Page search(Search q) { return new Page(mapper.search(q),mapper.count(q),q.page(),q.size()); }
 public List<Department> departments() { return mapper.departments(); }
 public State state(String id) { return mapper.state(id); }
 @Transactional public void create(Create e,String actor) {
  try { if(mapper.create(e)!=1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST); }
  catch(DuplicateKeyException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT); }
  record(new PersonnelEvent(actor,"EMPLOYEE_CREATED",e.employeeId()));
 }
 @Transactional public void change(String id,String status,String dept,String actor) {
  if(mapper.lockEmployee(id)==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  if(mapper.change(id,status,dept)!=1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  record(new PersonnelEvent(actor,dept==null?"EMPLOYEE_STATUS_CHANGED":"EMPLOYEE_DEPARTMENT_CHANGED",id));
 }
}
