package egovframework.example.document.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.document.repository.InMemoryDocumentWorkspace;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ManagedDocumentMockMvcTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder passwords;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired InMemoryDocumentWorkspace docs;
    @BeforeEach void reset(){accounts.resetForTest();docs.resetForTest();}
    private MockHttpSession login(String code) throws Exception {
        String password=UUID.randomUUID()+"Aa1!";accounts.provisionForTest(code,passwords.encode(password));
        var result=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content(json.writeValueAsString(Map.of("employeeCode",code,"password",password))))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession)result.getRequest().getSession(false);
    }
    private String input(){return "{\"documentCode\":\"DOC-MVC\",\"title\":\"Synthetic\",\"category\":\"ONBOARDING\",\"scope\":\"SELECTED\",\"format\":\"MARKDOWN\",\"body\":\"# Safe\\n<script>x</script>\",\"aiSearchable\":true,\"externalAiAllowed\":false}";}
    @Test void publishRequiresAssignedManagerAndGrantRevokeHidesImmediately() throws Exception {
        mvc.perform(get("/api/documents/DOC-MVC/published")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/documents/search").param("q","Safe")).andExpect(status().isUnauthorized());
        var editor=login("EMP-005");var manager=login("EMP-002");var viewer=login("EMP-010");var admin=login("EMP-001");
        mvc.perform(post("/api/documents").session(editor).contentType("application/json").content(input())).andExpect(status().isForbidden());
        mvc.perform(post("/api/documents").session(editor).with(csrf()).contentType("application/json").content(input())).andExpect(status().isOk());
        mvc.perform(get("/api/documents/DOC-MVC/published").session(manager)).andExpect(status().isNotFound());
        for(String permission:List.of("READ","APPROVE")){
            mvc.perform(post("/api/documents/DOC-MVC/grants").session(editor).with(csrf()).contentType("application/json")
                    .content("{\"employeeId\":\"EMP-002\",\"permission\":\""+permission+"\"}"))
                    .andExpect(status().isOk());
        }
        mvc.perform(post("/api/documents/DOC-MVC/review-request").session(editor).with(csrf()).contentType("application/json")
                .content("{\"reviewerEmployeeId\":\"EMP-002\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/documents/DOC-MVC/approve").session(admin).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/api/documents/DOC-MVC/approve").session(manager).with(csrf())).andExpect(status().isOk());
        var response=mvc.perform(get("/api/documents/DOC-MVC/published").session(manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.versionNo").value(1)).andReturn();
        assertThat(response.getResponse().getContentAsString()).doesNotContain("<script>","<img").contains("&lt;script&gt;");
        mvc.perform(get("/api/documents/DOC-MVC/published").session(viewer)).andExpect(status().isNotFound());
        mvc.perform(post("/api/documents/DOC-MVC/grants").session(editor).with(csrf()).contentType("application/json")
                .content("{\"employeeId\":\"EMP-010\",\"permission\":\"READ\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/documents/DOC-MVC/published").session(viewer)).andExpect(status().isOk());
        var searchResponse=mvc.perform(get("/api/documents/search").param("q","Safe").param("size","1").session(viewer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(1)).andReturn();
        var hit=json.readTree(searchResponse.getResponse().getContentAsString()).path("data").path("items").get(0);
        String sourcePath="/api/documents/"+hit.path("documentId").asLong()+"/versions/"+hit.path("versionId").asLong()+"/fragments/"+hit.path("fragmentId").asLong();
        mvc.perform(get(sourcePath).session(viewer)).andExpect(status().isOk()).andExpect(jsonPath("$.data.content").value("# Safe\n<script>x</script>"));
        mvc.perform(get("/api/documents/search").param("q","' OR 1=1 --").session(viewer)).andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(0));
        mvc.perform(get("/api/documents/search").param("q","Safe").param("size","51").session(viewer)).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/documents/DOC-MVC/grants").session(editor).with(csrf()).contentType("application/json")
                .content("{\"employeeId\":\"EMP-010\",\"permission\":\"READ\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/documents/DOC-MVC/published").session(viewer)).andExpect(status().isNotFound());
        mvc.perform(get(sourcePath).session(viewer)).andExpect(status().isNotFound());
        mvc.perform(get("/api/documents/search").param("q","Safe").session(viewer)).andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(0));
        assertThat(docs.eventsForTest()).allSatisfy(e->assertThat(e.toString()).doesNotContain("<script>","# Safe"));
    }
}
