package egovframework.example.organization.repository;
import egovframework.example.organization.dto.DirectoryModels.*;
import egovframework.example.organization.domain.Department;
import egovframework.example.organization.fixture.SyntheticOrganizationFixture;
import egovframework.example.audit.domain.PersonnelEvent;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Repository @Profile({"test", "local-mock"})
public class InMemoryOrganizationRepository implements OrganizationRepository {
 private record Row(EmployeeView view, String task, String status) {}
 private final Map<String,Row> rows = new TreeMap<>();
 private final List<PersonnelEvent> events = new ArrayList<>();
 public InMemoryOrganizationRepository() { resetForTest(); }
 public synchronized void resetForTest() {
  rows.clear(); events.clear();
  for(var e:SyntheticOrganizationFixture.employees()) rows.put(e.employeeId(),new Row(new EmployeeView(e.employeeId(),e.displayName(),e.departmentCode(),e.jobTitle(),e.companyEmail(),e.extensionNo()),e.dutySummary(),"ACTIVE"));
 }
 public synchronized List<PersonnelEvent> eventsForTest() { return List.copyOf(events); }
 public synchronized Page search(Search q) {
  Comparator<EmployeeView> order = switch(q.sort()) {
   case "name" -> Comparator.comparing(EmployeeView::displayName);
   case "departmentId" -> Comparator.comparing(EmployeeView::departmentCode);
   default -> Comparator.comparing(EmployeeView::employeeId);
  };
  var found=rows.values().stream().filter(r->!r.status.equals("DEPARTED"))
   .filter(r->r.view.displayName().contains(q.name()) && (q.departmentId().isEmpty() || r.view.departmentCode().equals(q.departmentId())) && r.task.contains(q.task()))
   .map(Row::view).sorted(order.thenComparing(EmployeeView::employeeId)).toList();
  return new Page(found.stream().skip(q.offset()).limit(q.size()).toList(),found.size(),q.page(),q.size());
 }
 public List<Department> departments() { return SyntheticOrganizationFixture.departments(); }
 private void department(String code) { if(departments().stream().noneMatch(d->d.departmentCode().equals(code))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST); }
 public synchronized void create(Create e,String actor) {
  department(e.departmentId());
  if(rows.containsKey(e.employeeId()) || rows.values().stream().anyMatch(r->r.view.companyEmail().equals(e.companyEmail()))) throw new ResponseStatusException(HttpStatus.CONFLICT);
  rows.put(e.employeeId(),new Row(new EmployeeView(e.employeeId(),e.displayName(),e.departmentId(),e.jobTitle(),e.companyEmail(),e.extensionNo()),e.task(),"ACTIVE"));
  events.add(new PersonnelEvent(actor,"EMPLOYEE_CREATED",e.employeeId()));
 }
 public synchronized void change(String id,String status,String dept,String actor) {
  var r=rows.get(id); if(r==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  if(dept!=null) department(dept);
  var e=r.view;
  rows.put(id,new Row(new EmployeeView(id,e.displayName(),dept==null?e.departmentCode():dept,e.jobTitle(),e.companyEmail(),e.extensionNo()),r.task,status==null?r.status:status));
  events.add(new PersonnelEvent(actor,dept==null?"EMPLOYEE_STATUS_CHANGED":"EMPLOYEE_DEPARTMENT_CHANGED",id));
 }
 public synchronized State state(String id) { var r=rows.get(id); return r==null?null:new State(r.view.departmentCode(),r.status); }
}
