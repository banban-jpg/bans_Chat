package egovframework.example.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.CredentialStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.auth.repository.InMemoryActivationRepository;
import egovframework.example.auth.service.AccountAuthenticationService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AuthFlowMockMvcTest.TestClock.class)
class AuthFlowMockMvcTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired InMemoryActivationRepository activations;
    @Autowired AccountAuthenticationService service;
    @Autowired PasswordEncoder passwords;
    @Autowired MutableClock clock;

    @BeforeEach
    void reset() {
        accounts.resetForTest();
        activations.resetForTest();
        clock.set(Instant.parse("2026-11-03T00:00:00Z"));
    }

    @Test
    void activationIsOneUseAndLoginLogoutMeUseRealSessionChain() throws Exception {
        provisionAdmin();
        String code = service.issueActivation(admin(), "EMP-002");
        String password = randomPassword();
        CsrfSession browser = browser();
        mvc.perform(send(post("/api/auth/activate"), browser,
                        Map.of("employeeCode", "EMP-002", "code", code, "newPassword", password)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.activated").value(true));
        mvc.perform(send(post("/api/auth/activate"), browser,
                        Map.of("employeeCode", "EMP-002", "code", code, "newPassword", password)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ACTIVATION_CONFLICT"));
        String oldSessionId = browser.session().getId();
        var result = mvc.perform(send(post("/api/auth/login"), browser,
                        Map.of("employeeCode", "EMP-002", "password", password)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.employeeCode").value("EMP-002"))
                .andReturn();
        assertThat(browser.session().getId()).isNotEqualTo(oldSessionId);
        assertThat(browser.session().getMaxInactiveInterval()).isEqualTo(1800);
        assertThat(result.getResponse().getContentAsString()).doesNotContain(password, code, "passwordHash");
        mvc.perform(get("/api/auth/me").session(browser.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.employeeCode").value("EMP-002"));
        mvc.perform(send(post("/api/auth/logout"), browser, null))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.loggedOut").value(true));
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void reissueInvalidatesOldCodeAndExpiredCodeIsRejected() throws Exception {
        provisionAdmin();
        String old = service.issueActivation(admin(), "EMP-005");
        String current = service.issueActivation(admin(), "EMP-005");
        assertThat(old).isNotEqualTo(current);
        CsrfSession browser = browser();
        String password = randomPassword();
        mvc.perform(send(post("/api/auth/activate"), browser,
                        Map.of("employeeCode", "EMP-005", "code", old, "newPassword", password)))
                .andExpect(status().isConflict());
        clock.advance(Duration.ofHours(24));
        mvc.perform(send(post("/api/auth/activate"), browser,
                        Map.of("employeeCode", "EMP-005", "code", current, "newPassword", password)))
                .andExpect(status().isConflict());
    }

    @Test
    void csrfIsRequiredAndFiveFailuresLockAccount() throws Exception {
        String password = randomPassword();
        accounts.provisionForTest("EMP-001", passwords.encode(password));
        CsrfSession browser = browser();
        mvc.perform(post("/api/auth/login").session(browser.session()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("employeeCode", "EMP-001", "password", password))))
                .andExpect(status().isForbidden());
        for (int i = 0; i < 4; i++) {
            mvc.perform(send(post("/api/auth/login"), browser,
                            Map.of("employeeCode", "EMP-001", "password", "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(send(post("/api/auth/login"), browser,
                        Map.of("employeeCode", "EMP-001", "password", "wrong-password")))
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.code").value("RATE_LIMITED"));
        mvc.perform(send(post("/api/auth/login"), browser,
                        Map.of("employeeCode", "EMP-001", "password", password)))
                .andExpect(status().isTooManyRequests());
        assertThat(accounts.find("EMP-001").orElseThrow().failedLoginCount()).isEqualTo(5);
    }

    @Test
    void inactiveAccountAndCredentialChangeInvalidateSession() throws Exception {
        String password = randomPassword();
        accounts.provisionForTest("EMP-001", passwords.encode(password));
        CsrfSession browser = browser();
        mvc.perform(send(post("/api/auth/login"), browser,
                        Map.of("employeeCode", "EMP-001", "password", password)))
                .andExpect(status().isOk());
        accounts.setStatusForTest("EMP-001", CredentialStatus.SUSPENDED);
        mvc.perform(get("/api/auth/me").session(browser.session())).andExpect(status().isUnauthorized());

        accounts.provisionForTest("EMP-001", passwords.encode(password));
        CsrfSession second = browser();
        mvc.perform(send(post("/api/auth/login"), second,
                        Map.of("employeeCode", "EMP-001", "password", password)))
                .andExpect(status().isOk());
        accounts.provisionForTest("EMP-001", passwords.encode(randomPassword()));
        mvc.perform(get("/api/auth/me").session(second.session())).andExpect(status().isUnauthorized());
    }

    private void provisionAdmin() {
        accounts.provisionForTest("EMP-001", passwords.encode(randomPassword()));
    }

    private SessionActor admin() {
        return new SessionActor("EMP-001", "D01", AccountStatus.ACTIVE,
                Set.of(SecurityRole.EMPLOYEE, SecurityRole.ACCOUNT_ADMIN));
    }

    private String randomPassword() { return UUID.randomUUID().toString() + "Aa1!"; }

    private CsrfSession browser() throws Exception {
        var result = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isNotEmpty()).andReturn();
        String token = json.readTree(result.getResponse().getContentAsString()).at("/data/token").asText();
        return new CsrfSession((MockHttpSession) result.getRequest().getSession(false), token);
    }

    private MockHttpServletRequestBuilder send(MockHttpServletRequestBuilder request, CsrfSession browser,
                                               Map<String, String> body) throws Exception {
        request.session(browser.session()).header("X-CSRF-TOKEN", browser.token());
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        return request;
    }

    private record CsrfSession(MockHttpSession session, String token) { }

    @TestConfiguration
    static class TestClock {
        @Bean @Primary MutableClock mutableClock() { return new MutableClock(); }
    }

    static class MutableClock extends Clock {
        private final AtomicReference<Instant> now = new AtomicReference<>(Instant.EPOCH);
        void set(Instant value) { now.set(value); }
        void advance(Duration amount) { now.updateAndGet(value -> value.plus(amount)); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now.get(); }
    }
}
