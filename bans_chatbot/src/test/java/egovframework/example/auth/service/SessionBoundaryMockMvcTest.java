package egovframework.example.auth.service;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local-mock")
class SessionBoundaryMockMvcTest {
    @Autowired MockMvc mvc;

    @Test
    void anonymousDemoRequestIsDeniedWithPublicErrorContract() throws Exception {
        mvc.perform(get("/api/demo/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void suspendedSessionIsForbidden() throws Exception {
        mvc.perform(get("/api/demo/employees").session(session(AccountStatus.SUSPENDED)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void activeServerSessionCanUseDemoRouteAndRequestUserIdIsIgnored() throws Exception {
        mvc.perform(get("/api/demo/employees").param("userId", "EMP-999")
                        .session(session(AccountStatus.ACTIVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(20));
    }

    @Test
    void healthIsAnonymous() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    private MockHttpSession session(AccountStatus status) {
        var session = new MockHttpSession();
        session.setAttribute(SessionBoundaryInterceptor.SESSION_ACTOR,
                new SessionActor("EMP-001", "D01", status, Set.of(SecurityRole.EMPLOYEE)));
        return session;
    }
}
