package egovframework.example.organization.service;
import egovframework.example.organization.repository.OrganizationRepository;
import egovframework.example.organization.dto.DirectoryModels.*;
import egovframework.example.organization.domain.Department;
import egovframework.example.auth.domain.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
public class OrganizationService {
 private final OrganizationRepository repository;
 public OrganizationService(OrganizationRepository repository) { this.repository=repository; }
 private void active(SessionActor actor) { if(actor==null || !actor.activeEmployee()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED); }
 private void admin(SessionActor actor) { active(actor); if(!actor.has(SecurityRole.ACCOUNT_ADMIN)) throw new ResponseStatusException(HttpStatus.FORBIDDEN); }
 public Page search(SessionActor actor,Search q) { active(actor); validate(q); return repository.search(q); }
 public List<Department> departments(SessionActor actor) { active(actor); return repository.departments(); }
 /** Only unchanged canonical invented public fields may cross the external AI boundary. */
 public List<EmployeeView> syntheticForExternalAi(SessionActor actor,Search q) {
  return search(actor,q).items().stream().filter(view->egovframework.example.organization.fixture.SyntheticOrganizationFixture.employees().stream()
   .anyMatch(e->new EmployeeView(e.employeeId(),e.displayName(),e.departmentCode(),e.jobTitle(),e.companyEmail(),e.extensionNo()).equals(view)
    &&e.dutySummary().contains(q.task()))).toList();
 }
 public List<Department> syntheticDepartmentForExternalAi(SessionActor actor) {
  return departments(actor).stream().filter(d->d.departmentCode().equals(actor.departmentCode())
   &&egovframework.example.organization.fixture.SyntheticOrganizationFixture.departments().contains(d)).toList();
 }
 public void create(SessionActor actor,Create body) { admin(actor); repository.create(body,actor.employeeId()); }
 public void change(SessionActor actor,String id,String status,String department) { admin(actor); if(!id.matches("EMP-[0-9]{3,10}")) bad(); if((status==null)==(department==null)) bad(); if(status!=null && !List.of("ACTIVE","SUSPENDED","DEPARTED").contains(status)) bad(); if(department!=null && !department.matches("D[0-9]{2}")) bad(); repository.change(id,status,department,actor.employeeId()); }
 private static void bad() { throw new ResponseStatusException(HttpStatus.BAD_REQUEST); }
 public static void validate(Search q) {
  if(q.page()<0 || q.page()>10000 || q.size()<1 || q.size()>100 || !List.of("employeeId","name","departmentId").contains(q.sort())) bad();
  for(String s:List.of(q.name(),q.task())) if(s.length()>100 || s.chars().anyMatch(Character::isISOControl)) bad();
  if(!q.departmentId().isEmpty() && !q.departmentId().matches("D[0-9]{2}")) bad();
 }
}
