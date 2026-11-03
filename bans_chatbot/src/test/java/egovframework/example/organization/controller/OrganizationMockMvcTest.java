package egovframework.example.organization.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.organization.repository.InMemoryOrganizationRepository;
import egovframework.example.document.service.DocumentAccessPolicy;
import egovframework.example.document.domain.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class OrganizationMockMvcTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Autowired InMemoryAccountRepository accounts;
 @Autowired InMemoryOrganizationRepository organization;
 @Autowired PasswordEncoder passwords;
 @BeforeEach void reset() { accounts.resetForTest(); organization.resetForTest(); }
 MockHttpSession login(String code) throws Exception {
  String password=UUID.randomUUID()+"Aa1!";
  accounts.provisionForTest(code,passwords.encode(password));
  var result=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
   .content(json.writeValueAsString(Map.of("employeeCode",code,"password",password)))).andExpect(status().isOk()).andReturn();
  return (MockHttpSession)result.getRequest().getSession(false);
 }
 @Test void searchFiltersPagesAndExposesOnlySixPublicFields() throws Exception {
  var user=login("EMP-002");
  var result=mvc.perform(get("/api/employees").session(user).param("departmentId","D01").param("size","2"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(4)).andExpect(jsonPath("$.data.items.length()").value(2))
   .andExpect(jsonPath("$.data.items[0].departmentCode").value("D01")).andReturn();
  var item=json.readTree(result.getResponse().getContentAsString()).path("data").path("items").get(0);
  assertThat(item.size()).isEqualTo(6);
  assertThat(item.has("passwordHash")).isFalse(); assertThat(item.has("dutySummary")).isFalse();
  mvc.perform(get("/api/departments").session(user)).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(5));
  mvc.perform(get("/api/tasks/owners").session(user).param("query","' OR 1=1 --")).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
  mvc.perform(get("/api/employees").session(user).param("name","' OR 1=1 --")).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
  for(var params:List.of(Map.of("size","101"),Map.of("page","10001"),Map.of("page","oops"),Map.of("sort","name DESC;DROP TABLE employee"),Map.of("name","x".repeat(101)),Map.of("departmentId","D01' OR 1=1"))) {
   var request=get("/api/employees").session(user); params.forEach(request::param); mvc.perform(request).andExpect(status().isBadRequest());
  }
 }
 @Test void authenticationAdminAndCsrfAreEnforced() throws Exception {
  mvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
  mvc.perform(patch("/api/admin/employees/EMP-003/status").with(csrf()).contentType("application/json").content("{\"status\":\"SUSPENDED\"}")).andExpect(status().isUnauthorized());
  var user=login("EMP-002");
  mvc.perform(patch("/api/admin/employees/EMP-003/status").session(user).with(csrf()).contentType("application/json").content("{\"status\":\"SUSPENDED\"}")).andExpect(status().isForbidden());
  var admin=login("EMP-001");
  mvc.perform(patch("/api/admin/employees/EMP-003/status").session(admin).contentType("application/json").content("{\"status\":\"SUSPENDED\"}")).andExpect(status().isForbidden());
  assertThat(organization.eventsForTest()).isEmpty();
 }
 @Test void moveRefreshesSessionActorAndStatusRevokesExistingSession() throws Exception {
  var admin=login("EMP-001"); var user=login("EMP-002");
  var oldDepartmentDocument=new DocumentRecord("DOC-TEST","EMP-020","D01",DocumentScope.DEPARTMENT,true);
  var newDepartmentDocument=new DocumentRecord("DOC-TEST-2","EMP-020","D02",DocumentScope.DEPARTMENT,true);
  var policy=new DocumentAccessPolicy((document,employee,permission)->false);
  assertThat(policy.canRead(accounts.find("EMP-002").orElseThrow().actor(),oldDepartmentDocument)).isTrue();
  assertThat(policy.canRead(accounts.find("EMP-002").orElseThrow().actor(),newDepartmentDocument)).isFalse();
  mvc.perform(patch("/api/admin/employees/EMP-002/department").session(admin).with(csrf()).contentType("application/json").content("{\"departmentId\":\"D02\"}")).andExpect(status().isOk());
  mvc.perform(get("/api/auth/me").session(user)).andExpect(status().isOk()).andExpect(jsonPath("$.data.departmentCode").value("D02"));
  assertThat(accounts.find("EMP-002").orElseThrow().actor().departmentCode()).isEqualTo("D02");
  var request=mvc.perform(get("/api/auth/me").session(user)).andReturn().getRequest();
  var refreshed=(egovframework.example.auth.domain.SessionActor)request.getAttribute(egovframework.example.auth.service.SessionBoundaryInterceptor.SESSION_ACTOR);
  assertThat(policy.canRead(refreshed,oldDepartmentDocument)).isFalse();
  assertThat(policy.canRead(refreshed,newDepartmentDocument)).isTrue();
  mvc.perform(patch("/api/admin/employees/EMP-002/status").session(admin).with(csrf()).contentType("application/json").content("{\"status\":\"DEPARTED\"}")).andExpect(status().isOk());
  mvc.perform(get("/api/employees").session(user)).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/employees").session(admin)).andExpect(jsonPath("$.data.total").value(19));
  assertThat(organization.eventsForTest()).hasSize(2);
 }
 @Test void createsSyntheticEmployeeRejectsPrivilegeFieldsAndInvalidChanges() throws Exception {
  var admin=login("EMP-001");
  var body=new LinkedHashMap<String,Object>(Map.of("employeeId","EMP-021","displayName","가상신입","departmentId","D01","jobTitle","사원","companyEmail","new@bans.example","extensionNo","1021","task","가상업무"));
  body.put("roles",List.of("ACCOUNT_ADMIN"));
  mvc.perform(post("/api/admin/employees").session(admin).with(csrf()).contentType("application/json").content(json.writeValueAsString(body))).andExpect(status().isBadRequest());
  body.remove("roles");
  mvc.perform(post("/api/admin/employees").session(admin).with(csrf()).contentType("application/json").content(json.writeValueAsString(body))).andExpect(status().isCreated());
  mvc.perform(post("/api/admin/employees").session(admin).with(csrf()).contentType("application/json").content(json.writeValueAsString(body))).andExpect(status().isConflict());
  mvc.perform(patch("/api/admin/employees/EMP-021/department").session(admin).with(csrf()).contentType("application/json").content("{\"departmentId\":\"D99\"}")).andExpect(status().isBadRequest());
  mvc.perform(patch("/api/admin/employees/EMP-999/status").session(admin).with(csrf()).contentType("application/json").content("{\"status\":\"ACTIVE\"}")).andExpect(status().isNotFound());
  assertThat(accounts.find("EMP-021")).isEmpty();
  assertThat(organization.eventsForTest()).hasSize(1);
 }
 @Test void suspendedEmployeeCannotReuseSessionOrLogin() throws Exception {
  var admin=login("EMP-001");
  String password=UUID.randomUUID()+"Aa1!";
  accounts.provisionForTest("EMP-010",passwords.encode(password));
  var result=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(json.writeValueAsString(Map.of("employeeCode","EMP-010","password",password)))).andExpect(status().isOk()).andReturn();
  var user=(MockHttpSession)result.getRequest().getSession(false);
  mvc.perform(patch("/api/admin/employees/EMP-010/status").session(admin).with(csrf()).contentType("application/json").content("{\"status\":\"SUSPENDED\"}")).andExpect(status().isOk());
  mvc.perform(get("/api/employees").session(user)).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(json.writeValueAsString(Map.of("employeeCode","EMP-010","password",password)))).andExpect(status().isUnauthorized());
 }

}
