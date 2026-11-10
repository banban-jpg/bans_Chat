package egovframework.example.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.ai.provider.*;
import egovframework.example.audit.repository.InMemoryAiQuestionAudit;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.organization.repository.InMemoryOrganizationRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles({"test", "fixture-test"})
class AiQuestionMockMvcTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired InMemoryOrganizationRepository directory;
    @Autowired InMemoryAiQuestionAudit audit;
    @Autowired PasswordEncoder passwords;
    @MockitoSpyBean ChatProvider provider;
    @BeforeEach void reset() { directory.resetForTest(); accounts.resetForTest(); audit.resetForTest(); }
    MockHttpSession login() throws Exception {
        String password = UUID.randomUUID() + "Aa1!"; accounts.provisionForTest("EMP-002", passwords.encode(password));
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content(json.writeValueAsString(Map.of("employeeCode", "EMP-002", "password", password))))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
    String body(String question) throws Exception { return json.writeValueAsString(Map.of("question", question)); }
    @Test void authenticationCsrfAndUiNoticeAreEnforced() throws Exception {
        mvc.perform(post("/api/ai/questions").with(csrf()).contentType("application/json").content(body("연차 신청 절차"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/ai/questions")).andExpect(status().isUnauthorized());
        var session = login();
        mvc.perform(post("/api/ai/questions").session(session).contentType("application/json").content(body("연차 신청 절차"))).andExpect(status().isForbidden());
        var page = mvc.perform(get("/ai/questions").session(session)).andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store")).andReturn();
        assertThat(page.getResponse().getContentAsString()).contains("질문 원문도", "개인정보", "textContent", "type=\"checkbox\"")
                .doesNotContain("innerHTML", "localStorage", "GOOGLE_API_KEY");
    }
    @Test void vacationReturnsSourceAndServerRequestIdWithoutRawModelConversation() throws Exception {
        var session = login();
        var response = mvc.perform(post("/api/ai/questions").session(session).with(csrf()).header("X-Request-Id", "FORGED")
                .contentType("application/json").content(body("연차 신청 절차")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.answerType").value("DOCUMENT"))
                .andExpect(jsonPath("$.data.sources.length()").value(1)).andExpect(jsonPath("$.data.structuredFacts.length()").value(0))
                .andExpect(header().string("Cache-Control", "no-store")).andReturn().getResponse();
        var node = json.readTree(response.getContentAsString()).path("data");
        assertThat(node.size()).isEqualTo(5); assertThat(node.path("requestId").asText()).isEqualTo(response.getHeader("X-Request-Id")).isNotEqualTo("FORGED");
        assertThat(response.getContentAsString()).doesNotContain("\"question\"", "\"quote\"", "evidenceId", "FORGED");
        mvc.perform(get(node.path("sources").get(0).path("url").asText()).session(session)).andExpect(status().isOk());
        assertThat(audit.eventsForTest().toString()).doesNotContain("연차 신청 절차", "합성 직원");
    }
    @Test void expenseOwnerUsesExactPublicFieldsAndNoProvider() throws Exception {
        mvc.perform(post("/api/ai/questions").session(login()).with(csrf()).contentType("application/json").content(body("출장비 담당자")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.answerType").value("STRUCTURED"))
                .andExpect(jsonPath("$.data.structuredFacts[0].employeeId").value("EMP-005"))
                .andExpect(jsonPath("$.data.structuredFacts[0].extensionNo").value("2201"))
                .andExpect(jsonPath("$.data.structuredFacts[0].companyEmail").value("emp005@bans.example"));
        verify(provider, never()).grounded(any(), any());
    }
    @ParameterizedTest @ValueSource(strings={"폐기된 장비 안내", "비존재 규정", "배포 절차", "RAW_PRIVATE_QUESTION_SENTINEL"})
    void unknownDiscardedAndUnauthorizedReturnUnanswerableWithoutLeakingExistence(String question) throws Exception {
        var request = Map.of("question", question, "callerId", "EMP-008", "role", "ACCOUNT_ADMIN", "departmentId", "D03");
        var response = mvc.perform(post("/api/ai/questions").session(login()).with(csrf()).contentType("application/json").content(json.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.answerType").value("UNANSWERABLE"))
                .andExpect(jsonPath("$.data.sources.length()").value(0)).andExpect(jsonPath("$.data.structuredFacts.length()").value(0)).andReturn().getResponse();
        assertThat(response.getContentAsString()).doesNotContain(question); verify(provider, never()).grounded(any(), any());
    }
    @ParameterizedTest @ValueSource(strings={"UPSTREAM_RATE_LIMITED", "UPSTREAM_UNAVAILABLE", "TIMEOUT"})
    void aiFailureDoesNotBreakChatRestOrTheAuthenticatedSession(String kind) throws Exception {
        var session = login();
        doThrow(new ProviderFailure(ProviderFailure.Kind.valueOf(kind))).when(provider).grounded(any(), any());
        int expected = kind.equals("UPSTREAM_RATE_LIMITED") ? 429 : kind.equals("TIMEOUT") ? 504 : 503;
        var result = mvc.perform(post("/api/ai/questions").session(session).with(csrf()).contentType("application/json").content(body("연차 신청 절차")))
                .andExpect(status().is(expected)).andExpect(jsonPath("$.code").value("AI_"+kind)).andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("연차 신청 절차", "合成", "quote");
        mvc.perform(get("/api/chat/rooms").session(session)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk());
    }
    @Test void invalidBodyAndPerSessionQuestionLimitAreSafe() throws Exception {
        var session = login();
        mvc.perform(post("/api/ai/questions").session(session).with(csrf()).contentType("application/json").content("x".repeat(2049)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("AI_QUESTION_INPUT_INVALID"));
        for(int i=0;i<4;i++) mvc.perform(post("/api/ai/questions").session(session).with(csrf()).contentType("application/json").content(body("비존재 규정"))).andExpect(status().isOk());
        mvc.perform(post("/api/ai/questions").session(session).with(csrf()).contentType("application/json").content(body("비존재 규정")))
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.code").value("AI_TOOL_LIMIT"));
    }
}
