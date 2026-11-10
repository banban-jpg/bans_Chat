package egovframework.example.common.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;

/** Loopback HTTP, synthetic ephemeral stores and Mock Gemini. No response bodies/secrets in evidence. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "fixture-test"})
@DirtiesContext
class FinalApiHttpVerificationTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired PasswordEncoder passwords;
    @Autowired @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mappings;
    private final List<Map<String,Object>> checks = new ArrayList<>();
    private final Set<String> covered = new TreeSet<>();

    private final class Browser implements AutoCloseable {
        final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                .cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
        String csrfHeader, csrfToken;
        JsonNode call(String method,String route,String template,Object body,int expected) throws Exception {
            var builder=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+route)).timeout(Duration.ofSeconds(10));
            if(!method.equals("GET") && csrfToken!=null) builder.header(csrfHeader,csrfToken);
            if(body!=null)builder.header("Content-Type","application/json");
            builder.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
            var response=http.send(builder.build(),HttpResponse.BodyHandlers.ofString());
            checks.add(Map.of("method",method,"path",template,"expected",expected,"actual",response.statusCode()));
            assertThat(response.statusCode()).as(method+" "+template).isEqualTo(expected);
            if(expected>=200 && expected<300)covered.add(method+" "+template);
            if(!response.headers().firstValue("content-type").orElse("").contains("application/json"))return json.nullNode();
            JsonNode result=json.readTree(response.body());
            if(expected>=400)assertThat(result.hasNonNull("requestId")).isTrue();
            return result.path("data");
        }
        void csrf() throws Exception {
            var token=call("GET","/api/auth/csrf","/api/auth/csrf",null,200);
            csrfHeader=token.path("headerName").asText();csrfToken=token.path("token").asText();
        }
        void login(String employee,String password) throws Exception {
            csrf();call("POST","/api/auth/login","/api/auth/login",Map.of("employeeCode",employee,"password",password),200);csrf();
        }
        @Override public void close(){http.close();}
    }

    @Test @Timeout(90)
    void everyImplementedMvcMappingHasASuccessfulLoopbackRequestAndSecurityChecks() throws Exception {
        accounts.resetForTest();String password=UUID.randomUUID()+"Aa1!";
        for(String employee:List.of("EMP-001","EMP-002","EMP-006"))accounts.provisionForTest(employee,passwords.encode(password));
        var inventory=new ArrayList<Map<String,Object>>();
        mappings.getHandlerMethods().forEach((mapping,handler)->{
            if(!handler.getBeanType().getName().startsWith("egovframework.example."))return;
            for(var method:mapping.getMethodsCondition().getMethods())for(var path:mapping.getPatternValues())
                inventory.add(Map.of("method",method.name(),"path",path,"controller",handler.getBeanType().getName(),
                        "handler",handler.getMethod().getName(),"returnType",handler.getMethod().getGenericReturnType().getTypeName()));
        });
        try(var anonymous=new Browser();var admin=new Browser();var editor=new Browser();var manager=new Browser();var outsider=new Browser()) {
            for(String path:List.of("/","/health","/api/health"))anonymous.call("GET",path,path,null,200);
            for(String path:List.of("/ui/app.css","/ui/api.js","/ui/stomp.js","/ui/app.js"))anonymous.call("GET",path,path,null,200);
            anonymous.call("GET","/api/auth/me","/api/auth/me",null,401);
            anonymous.call("POST","/api/auth/login","/api/auth/login",Map.of("employeeCode","EMP-001","password",password),403);
            admin.login("EMP-001",password);manager.login("EMP-002",password);outsider.login("EMP-006",password);
            var issued=admin.call("POST","/api/admin/activations","/api/admin/activations",Map.of("employeeCode","EMP-005"),200);
            anonymous.csrf();anonymous.call("POST","/api/auth/activate","/api/auth/activate",Map.of("employeeCode","EMP-005","code",issued.path("code").asText(),"newPassword",password),200);
            editor.login("EMP-005",password);
            editor.call("GET","/api/auth/me","/api/auth/me",null,200);
            editor.call("GET","/api/employees?name=&page=0&size=10","/api/employees",null,200);
            editor.call("GET","/api/departments","/api/departments",null,200);
            editor.call("GET","/api/tasks/owners?query=%EC%A0%95%EC%82%B0","/api/tasks/owners",null,200);
            editor.call("GET","/api/demo/employees","/api/demo/employees",null,200);
            admin.call("POST","/api/admin/employees","/api/admin/employees",Map.of("employeeId","EMP-099","displayName","가상검증99","departmentId","D01","jobTitle","가상 담당","companyEmail","verify99@bans.example","extensionNo","1999","task","합성 검증"),201);
            admin.call("PATCH","/api/admin/employees/EMP-099/status","/api/admin/employees/{id}/status",Map.of("status","SUSPENDED"),200);
            admin.call("PATCH","/api/admin/employees/EMP-099/department","/api/admin/employees/{id}/department",Map.of("departmentId","D02"),200);
            outsider.call("PATCH","/api/admin/employees/EMP-099/status","/api/admin/employees/{id}/status",Map.of("status","ACTIVE"),403);
            var room=editor.call("POST","/api/chat/rooms","/api/chat/rooms",Map.of("recipientEmployeeId","EMP-002"),200);
            String roomPath="/api/chat/rooms/"+room.path("conversationId").asLong();
            editor.call("GET","/api/chat/rooms","/api/chat/rooms",null,200);
            editor.call("POST",roomPath+"/messages","/api/chat/rooms/{id}/messages",Map.of("clientRequestId","final-http-1","content","합성 검증 메시지"),200);
            var messages=manager.call("GET",roomPath+"/messages?size=20","/api/chat/rooms/{id}/messages",null,200);
            assertThat(messages.path("items").size()).isEqualTo(1);
            manager.call("PUT",roomPath+"/read-state","/api/chat/rooms/{id}/read-state",Map.of("lastReadSeq",1),200);
            outsider.call("GET",roomPath+"/messages","/api/chat/rooms/{id}/messages",null,404);
            String code="DOC-FINAL-HTTP",doc="/api/documents/"+code;
            editor.call("POST","/api/documents","/api/documents",Map.of("documentCode",code,"title","합성 최종 검증","category","ONBOARDING","scope","SELECTED","format","MARKDOWN","body","# 합성 검증\n<script>forbidden()</script>","aiSearchable",true,"externalAiAllowed",false),200);
            var version=Map.of("format","MARKDOWN","body","# 합성 검증\n<script>forbidden()</script>","aiSearchable",true,"externalAiAllowed",false);
            editor.call("PUT",doc+"/versions/draft","/api/documents/{code}/versions/draft",version,200);
            for(String permission:List.of("READ","APPROVE"))editor.call("POST",doc+"/grants","/api/documents/{code}/grants",Map.of("employeeId","EMP-002","permission",permission),200);
            editor.call("POST",doc+"/review-request","/api/documents/{code}/review-request",Map.of("reviewerEmployeeId","EMP-002"),200);
            manager.call("POST",doc+"/approve","/api/documents/{code}/approve",null,200);
            var published=manager.call("GET",doc+"/published","/api/documents/{code}/published",null,200);
            assertThat(published.path("renderedHtml").asText()).doesNotContain("<script>");
            outsider.call("GET",doc+"/published","/api/documents/{code}/published",null,404);
            var hits=manager.call("GET","/api/documents/search?q=%ED%95%A9%EC%84%B1","/api/documents/search",null,200);
            JsonNode hit=null;for(var candidate:hits.path("items"))if(candidate.path("title").asText().equals("합성 최종 검증"))hit=candidate;
            assertThat(hit).isNotNull();
            String source="/api/documents/"+hit.path("documentId").asLong()+"/versions/"+hit.path("versionId").asLong()+"/fragments/"+hit.path("fragmentId").asLong();
            manager.call("GET",source,"/api/documents/{id}/versions/{versionId}/fragments/{fragmentId}",null,200);
            editor.call("DELETE",doc+"/grants","/api/documents/{code}/grants",Map.of("employeeId","EMP-002","permission","READ"),200);
            manager.call("GET",doc+"/published","/api/documents/{code}/published",null,404);
            manager.call("GET",source,"/api/documents/{id}/versions/{versionId}/fragments/{fragmentId}",null,404);
            editor.call("POST",doc+"/grants","/api/documents/{code}/grants",Map.of("employeeId","EMP-002","permission","READ"),200);
            editor.call("POST",doc+"/versions","/api/documents/{code}/versions",version,200);
            editor.call("POST",doc+"/review-request","/api/documents/{code}/review-request",Map.of("reviewerEmployeeId","EMP-002"),200);
            manager.call("POST",doc+"/reject","/api/documents/{code}/reject",null,200);
            editor.call("POST",doc+"/withdraw","/api/documents/{code}/withdraw",null,200);
            for(String path:List.of("/api/ai/questions/examples","/api/ai/tools","/ai/questions","/ai/tools"))editor.call("GET",path,path,null,200);
            var answer=editor.call("POST","/api/ai/questions","/api/ai/questions",Map.of("question","출장비 담당자"),200);
            assertThat(answer.path("answerType").asText()).isEqualTo("STRUCTURED");
            editor.call("POST","/api/ai/tools/preview","/api/ai/tools/preview",Map.of("questionId","VACATION_HELP","question","가상 회사의 휴가 신청 안내를 찾아주세요.","externalTransferAcknowledged",true,"calls",List.of(Map.of("name","searchDocuments","arguments",Map.of("query","휴가","limit",3)))),200);
            editor.call("POST","/api/auth/logout","/api/auth/logout",null,200);
            editor.call("GET","/api/auth/me","/api/auth/me",null,401);
            Set<String> implemented=new TreeSet<>();for(var row:inventory)implemented.add(row.get("method")+" "+row.get("path"));
            assertThat(covered).containsAll(implemented);
            assertThat(covered.stream().filter(implemented::contains).count()).isEqualTo(implemented.size());
        } finally {
            Path evidence=Path.of("local-notes/step19-http-evidence.json");Files.createDirectories(evidence.getParent());
            json.writerWithDefaultPrettyPrinter().writeValue(evidence.toFile(),Map.of("time",java.time.OffsetDateTime.now().toString(),"profile","test","realOracleCalls",0,"realGeminiCalls",0,"inventory",inventory,"checks",checks,"covered",covered));
        }
    }
}
