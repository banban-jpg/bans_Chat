package egovframework.example.common.exception;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.service.PublicEndpointPolicy;
import egovframework.example.auth.service.SessionBoundaryInterceptor;
import egovframework.example.common.response.ApiResponse;
import egovframework.example.common.response.RequestIdFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApiContractMockMvcTest.ProbeController.class)
@Import({ApiExceptionHandler.class, RequestIdFilter.class, PublicEndpointPolicy.class,
        ApiContractMockMvcTest.ProbeController.class})
@ActiveProfiles("test")
class ApiContractMockMvcTest {
    @Autowired MockMvc mvc;

    @Test
    void successUsesDataEnvelope() throws Exception {
        mvc.perform(get("/api/contract-probe").session(session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value("ok"));
    }

    @Test
    void validationErrorHasServerRequestIdAndNoRejectedValue() throws Exception {
        var result = mvc.perform(post("/api/contract-probe").session(session())
                        .header(RequestIdFilter.HEADER, "client-chosen-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
                .andReturn();
        String generated = result.getResponse().getHeader(RequestIdFilter.HEADER);
        assertThat(generated).isNotBlank().isNotEqualTo("client-chosen-id");
        assertThat(result.getResponse().getContentAsString()).contains("\"requestId\":\"" + generated + "\"");
        assertThat(result.getResponse().getContentAsString()).doesNotContain("client-chosen-id");
    }

    @Test
    void unexpectedErrorHidesSensitiveMessage() throws Exception {
        var body = mvc.perform(get("/api/contract-probe/fail").session(session()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("secret-token", "database-password");
    }

    @Test
    void responseStatusReasonIsNotExposed() throws Exception {
        var body = mvc.perform(get("/api/contract-probe/forbidden").session(session()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("secret-token");
    }

    @Test
    void rateLimitReasonIsNotExposed() throws Exception {
        var body = mvc.perform(get("/api/contract-probe/rate").session(session()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("secret-token");
    }

    @Test
    void unknownApiPathUsesErrorEnvelope() throws Exception {
        mvc.perform(get("/api/does-not-exist").session(session()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @RestController
    static class ProbeController {
        @GetMapping("/api/contract-probe")
        ApiResponse<Map<String, String>> ok() {
            return ApiResponse.of(Map.of("value", "ok"));
        }

        @PostMapping("/api/contract-probe")
        ApiResponse<Map<String, String>> validate(@Valid @RequestBody ProbeRequest body) {
            return ApiResponse.of(Map.of("value", body.value()));
        }

        @GetMapping("/api/contract-probe/fail")
        void fail() {
            throw new IllegalStateException("secret-token database-password");
        }

        @GetMapping("/api/contract-probe/forbidden")
        void forbidden() {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "secret-token");
        }

        @GetMapping("/api/contract-probe/rate")
        void rate() {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "secret-token");
        }
    }

    record ProbeRequest(@NotBlank String value) {
    }

    private MockHttpSession session() {
        var session = new MockHttpSession();
        session.setAttribute(SessionBoundaryInterceptor.SESSION_ACTOR,
                new SessionActor("EMP-001", "D01", AccountStatus.ACTIVE, Set.of(SecurityRole.EMPLOYEE)));
        return session;
    }
}
