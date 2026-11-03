package egovframework.example.organization.controller;
import egovframework.example.organization.service.OrganizationService;
import egovframework.example.organization.dto.DirectoryModels.*;
import egovframework.example.organization.domain.Department;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.service.SessionBoundaryInterceptor;
import egovframework.example.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController
public class OrganizationController {
 private final OrganizationService service;
 private final org.springframework.core.env.Environment environment;
 public OrganizationController(OrganizationService service, org.springframework.core.env.Environment environment) {
  this.service=service; this.environment=environment;
 }
 @ModelAttribute
 public void labelSyntheticData(jakarta.servlet.http.HttpServletResponse response) {
  if(environment.acceptsProfiles(org.springframework.core.env.Profiles.of("local-mock"))) {
   response.setHeader("X-Demo-Data", "SYNTHETIC_EPHEMERAL");
  }
 }
 private SessionActor actor(HttpServletRequest r) { return (SessionActor)r.getAttribute(SessionBoundaryInterceptor.SESSION_ACTOR); }
 @GetMapping("/api/employees") public ApiResponse<Page> employees(HttpServletRequest r,
  @RequestParam(defaultValue="") String name,@RequestParam(defaultValue="") String departmentId,@RequestParam(defaultValue="") String task,
  @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="employeeId") String sort) {
  return ApiResponse.of(service.search(actor(r),new Search(name,departmentId,task,page,size,sort)));
 }
 @GetMapping("/api/departments") public ApiResponse<List<Department>> departments(HttpServletRequest r) { return ApiResponse.of(service.departments(actor(r))); }
 @GetMapping("/api/tasks/owners") public ApiResponse<Page> owners(HttpServletRequest r,@RequestParam String query) { return ApiResponse.of(service.search(actor(r),new Search("","",query,0,100,"employeeId"))); }
 @PostMapping("/api/admin/employees") @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<Boolean> create(HttpServletRequest r,@Valid @RequestBody Create body) { service.create(actor(r),body); return ApiResponse.of(true); }
 @PatchMapping("/api/admin/employees/{id}/status") public ApiResponse<Boolean> status(HttpServletRequest r,@PathVariable String id,@Valid @RequestBody Status body) { service.change(actor(r),id,body.status(),null); return ApiResponse.of(true); }
 @PatchMapping("/api/admin/employees/{id}/department") public ApiResponse<Boolean> department(HttpServletRequest r,@PathVariable String id,@Valid @RequestBody Move body) { service.change(actor(r),id,null,body.departmentId()); return ApiResponse.of(true); }
}
