package egovframework.example.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.ai.provider.MockGeminiProvider;
import egovframework.example.audit.repository.InMemoryAiToolAudit;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.organization.repository.InMemoryOrganizationRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles({"test", "fixture-test"})
class AiToolMockMvcTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired InMemoryOrganizationRepository directory;
    @Autowired PasswordEncoder passwords;
    @Autowired InMemoryAiToolAudit audit;
    @BeforeEach void reset() { directory.resetForTest(); accounts.resetForTest(); audit.resetForTest(); }
    private MockHttpSession login() throws Exception {
        String password = UUID.randomUUID() + "Aa1!"; accounts.provisionForTest("EMP-002", passwords.encode(password));
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content(json.writeValueAsString(Map.of("employeeCode", "EMP-002", "password", password))))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
    private String body() throws Exception {
        return json.writeValueAsString(Map.of("questionId", "VACATION_HELP", "question", "가상 회사의 휴가 신청 안내를 찾아주세요.",
                "externalTransferAcknowledged", true, "callerId", "EMP-008", "role", "ACCOUNT_ADMIN", "departmentId", "D03",
                "calls", List.of(Map.of("name", "searchDocuments", "arguments", Map.of("query", "휴가", "limit", 5)))));
    }
    @Test void authenticatedCatalogAndUiShowNoticeSchemasAndUnselectedConsent() throws Exception {
        mvc.perform(get("/api/ai/tools")).andExpect(status().isUnauthorized());
        mvc.perform(get("/ai/tools")).andExpect(status().isUnauthorized());
        var session = login();
        mvc.perform(get("/api/ai/tools").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tools.length()").value(5)).andExpect(jsonPath("$.data.mockOnly").value(true))
                .andExpect(jsonPath("$.data.limits.maxCalls").value(4));
        var page = mvc.perform(get("/ai/tools").session(session)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(page).contains("질문 원문도 외부로 전송", "개인정보", "type=\"checkbox\"", "textContent").doesNotContain("innerHTML", "GOOGLE_API_KEY", "checked>");
    }
    @Test void previewRequiresCsrfUsesServerSessionAndReturnsOnlyCountsAndNotice() throws Exception {
        var session = login();
        mvc.perform(post("/api/ai/tools/preview").session(session).contentType("application/json").content(body())).andExpect(status().isForbidden());
        var result = mvc.perform(post("/api/ai/tools/preview").session(session).with(csrf()).contentType("application/json").content(body()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.mock").value(true)).andExpect(jsonPath("$.data.returnedItems").value(1)).andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("합성 직원은", "questionId", "가상 회사의 휴가 신청 안내를 찾아주세요.", MockGeminiProvider.REPLY);
        assertThat(audit.eventsForTest().getFirst().actorEmployeeCode()).isEqualTo("EMP-002");
    }
    @Test void rejectsUnreviewedQuestionAndMissingAcknowledgementWithSanitizedErrors() throws Exception {
        var session = login();
        for (String input : List.of(body().replace("가상 회사의 휴가 신청 안내를 찾아주세요.", "RAW_PRIVATE_QUESTION"),
                body().replace("\"externalTransferAcknowledged\":true", "\"externalTransferAcknowledged\":false"))) {
            var result = mvc.perform(post("/api/ai/tools/preview").session(session).with(csrf()).contentType("application/json").content(input))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AI_QUESTION_NOT_REVIEWED")).andReturn();
            assertThat(result.getResponse().getContentAsString()).doesNotContain("RAW_PRIVATE_QUESTION");
        }
        assertThat(audit.eventsForTest().toString()).doesNotContain("RAW_PRIVATE_QUESTION");
    }
    @Test void oversizedBodiesAndUnregisteredMutationToolsAreRejected() throws Exception {
        var session = login();
        mvc.perform(post("/api/ai/tools/preview").session(session).with(csrf()).contentType("application/json").content("x".repeat(8193)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("AI_TOOL_INPUT_INVALID"));
        mvc.perform(post("/api/ai/tools/preview").session(session).with(csrf()).contentType("application/json").content(body().replace("searchDocuments", "executeSql")))
                .andExpect(status().isBadRequest());
    }
    @Test void sessionRevocationAndPerSessionRequestLimitApplyToRealSecurityChain() throws Exception {
        var session = login();
        for (int i = 0; i < 5; i++) mvc.perform(post("/api/ai/tools/preview").session(session).with(csrf()).contentType("application/json").content(body())).andExpect(status().isOk());
        mvc.perform(post("/api/ai/tools/preview").session(session).with(csrf()).contentType("application/json").content(body()))
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.code").value("AI_TOOL_LIMIT"));
        directory.change("EMP-002", "SUSPENDED", null, "EMP-001");
        mvc.perform(get("/api/ai/tools").session(session)).andExpect(status().isUnauthorized());
    }
}
