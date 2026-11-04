package egovframework.example.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("local-mock") @DirtiesContext
class WorkbenchMockMvcTest {
    @Autowired MockMvc mvc;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired PasswordEncoder passwords;
    @Autowired ObjectMapper json;
    @Autowired egovframework.example.organization.repository.InMemoryOrganizationRepository organization;
    @BeforeEach void reset(){organization.resetForTest();accounts.resetForTest();}
    @Test void publicShellHasSixScreensCspAndSmallSameOriginEscapedAssets() throws Exception {
        var shell=mvc.perform(get("/")).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"))
                .andExpect(header().string("Content-Security-Policy",org.hamcrest.Matchers.containsString("script-src 'self'"))).andReturn().getResponse();
        String html=shell.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(html).contains("가상 데이터 / 영구 저장 아님","개인정보·기밀 입력 금지","id=\"auth\"","id=\"directory\"","id=\"chat\"","id=\"documents\"","id=\"ai\"","id=\"admin\"","aria-live=\"polite\"");
        int bytes=shell.getContentAsByteArray().length;
        for(String path:List.of("/ui/app.js","/ui/api.js","/ui/stomp.js","/ui/app.css")){
            var response=mvc.perform(get(path)).andExpect(status().isOk()).andExpect(header().string("X-Content-Type-Options","nosniff")).andReturn().getResponse();
            bytes+=response.getContentAsByteArray().length;
            assertThat(response.getContentAsString()).doesNotContain("innerHTML","localStorage","sessionStorage","GOOGLE_API_KEY","generativelanguage.googleapis.com","console.log");
        }
        assertThat(bytes).isLessThan(65536);
        mvc.perform(get("/ui/private.json")).andExpect(status().isUnauthorized());
        mvc.perform(post("/").with(csrf())).andExpect(status().isUnauthorized());
    }
    @Test void onlyCurrentAdminCanIssueOneUseCodeAndRecipientUsesActivationApi() throws Exception {
        var admin=login("EMP-001");
        var result=mvc.perform(post("/api/admin/activations").session(admin).with(csrf()).contentType("application/json").content("{\"employeeCode\":\"EMP-005\"}"))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store")).andReturn();
        String code=json.readTree(result.getResponse().getContentAsString()).at("/data/code").asText();
        assertThat(code).hasSize(43);
        String password=UUID.randomUUID()+"Aa1!";
        String input=json.writeValueAsString(Map.of("employeeCode","EMP-005","code",code,"newPassword",password));
        mvc.perform(post("/api/auth/activate").with(csrf()).contentType("application/json").content(input)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/activate").with(csrf()).contentType("application/json").content(input)).andExpect(status().isConflict());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(json.writeValueAsString(Map.of("employeeCode","EMP-005","password",password))))
                .andExpect(status().isOk());
    }
    @Test void activationIssuanceRejectsNonAdminForgeryAndMissingCsrf() throws Exception {
        var manager=login("EMP-002");
        String forged="{\"employeeCode\":\"EMP-005\",\"role\":\"ACCOUNT_ADMIN\",\"callerId\":\"EMP-001\"}";
        mvc.perform(post("/api/admin/activations").session(manager).with(csrf()).contentType("application/json").content(forged)).andExpect(status().isForbidden());
        var admin=login("EMP-001");
        mvc.perform(post("/api/admin/activations").session(admin).contentType("application/json").content(forged)).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/activations").session(admin).with(csrf()).contentType("application/json").content("{\"employeeCode\":\"EMP-001\"}"))
                .andExpect(status().isForbidden());
    }
    @Test void sixScreenApiFlowActivatesSearchesChatsPublishesAnswersAndManagesEmployees() throws Exception {
        var admin=login("EMP-001");
        for(String employee:List.of("EMP-002","EMP-005")){
            var issued=mvc.perform(post("/api/admin/activations").session(admin).with(csrf()).contentType("application/json")
                    .content(json.writeValueAsString(Map.of("employeeCode",employee)))).andExpect(status().isOk()).andReturn();
            String code=json.readTree(issued.getResponse().getContentAsString()).at("/data/code").asText();
            String password=UUID.randomUUID()+"Aa1!";
            mvc.perform(post("/api/auth/activate").with(csrf()).contentType("application/json")
                    .content(json.writeValueAsString(Map.of("employeeCode",employee,"code",code,"newPassword",password)))).andExpect(status().isOk());
        }
        var editor=login("EMP-005");var manager=login("EMP-002");
        mvc.perform(get("/api/employees").session(editor).param("name","가상인사02")).andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(1));
        mvc.perform(get("/api/tasks/owners").session(editor).param("query","정산")).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].employeeId").value("EMP-005"));
        var opened=mvc.perform(post("/api/chat/rooms").session(editor).with(csrf()).contentType("application/json").content("{\"recipientEmployeeId\":\"EMP-002\"}"))
                .andExpect(status().isOk()).andReturn();
        String room=json.readTree(opened.getResponse().getContentAsString()).at("/data/conversationId").asText();
        mvc.perform(post("/api/chat/rooms/"+room+"/messages").session(editor).with(csrf()).contentType("application/json")
                .content("{\"clientRequestId\":\"ui-flow\",\"content\":\"가상 UI 대화\"}")).andExpect(status().isOk());
        mvc.perform(get("/api/chat/rooms/"+room+"/messages").session(manager)).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].content").value("가상 UI 대화"));
        mvc.perform(put("/api/chat/rooms/"+room+"/read-state").session(manager).with(csrf()).contentType("application/json").content("{\"lastReadSeq\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(1));
        mvc.perform(post("/api/documents").session(editor).with(csrf()).contentType("application/json")
                .content(json.writeValueAsString(Map.of("documentCode","DOC-UI-FLOW","title","UI-FLOW","category","ONBOARDING","scope","SELECTED","format","MARKDOWN","body","# UI-FLOW\n<script>alert(1)</script>","aiSearchable",true,"externalAiAllowed",false))))
                .andExpect(status().isOk());
        for(String permission:List.of("READ","APPROVE"))mvc.perform(post("/api/documents/DOC-UI-FLOW/grants").session(editor).with(csrf()).contentType("application/json")
                .content(json.writeValueAsString(Map.of("employeeId","EMP-002","permission",permission)))).andExpect(status().isOk());
        mvc.perform(post("/api/documents/DOC-UI-FLOW/review-request").session(editor).with(csrf()).contentType("application/json").content("{\"reviewerEmployeeId\":\"EMP-002\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/documents/DOC-UI-FLOW/approve").session(manager).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/documents/DOC-UI-FLOW/published").session(manager)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.renderedHtml",org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script>"))));
        mvc.perform(get("/api/documents/search").session(manager).param("q","UI-FLOW")).andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(1));
        mvc.perform(post("/api/ai/questions").session(manager).with(csrf()).contentType("application/json").content("{\"question\":\"출장비 담당자\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.answerType").value("STRUCTURED")).andExpect(jsonPath("$.data.structuredFacts[0].employeeId").value("EMP-005"));
        mvc.perform(patch("/api/admin/employees/EMP-005/department").session(admin).with(csrf()).contentType("application/json").content("{\"departmentId\":\"D01\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").session(editor)).andExpect(status().isOk()).andExpect(jsonPath("$.data.departmentCode").value("D01"));
    }
    private MockHttpSession login(String code) throws Exception {
        String password=UUID.randomUUID()+"Aa1!";accounts.provisionForTest(code,passwords.encode(password));
        return (MockHttpSession)mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content(json.writeValueAsString(Map.of("employeeCode",code,"password",password))))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
}
