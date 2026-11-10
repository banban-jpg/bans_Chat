package egovframework.example.auth.service;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.AuthenticatedEmployee;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import java.util.Set;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "fixture-test"})
class SessionBoundaryMockMvcTest {
    @Autowired MockMvc mvc;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired PasswordEncoder passwords;

    @BeforeEach
    void reset() { accounts.resetForTest(); }

    @Test
    void anonymousDemoRequestIsDeniedWithPublicErrorContract() throws Exception {
        mvc.perform(get("/api/demo/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void forgedSessionActorCannotBypassSecurityChain() throws Exception {
        mvc.perform(get("/api/demo/employees").session(session(AccountStatus.ACTIVE)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void activeServerSessionCanUseDemoRouteAndRequestUserIdIsIgnored() throws Exception {
        accounts.provisionForTest("EMP-001", passwords.encode(java.util.UUID.randomUUID().toString()));
        mvc.perform(get("/api/demo/employees").param("userId", "EMP-999")
                        .with(authentication(actorAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(20));
    }

    @Test
    void inactiveAccountRevokesAuthenticatedRequest() throws Exception {
        accounts.provisionForTest("EMP-001", passwords.encode(java.util.UUID.randomUUID().toString()));
        var old = actorAuthentication();
        accounts.setStatusForTest("EMP-001", egovframework.example.auth.domain.CredentialStatus.SUSPENDED);
        mvc.perform(get("/api/demo/employees").with(authentication(old)))
                .andExpect(status().isUnauthorized());
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

    private UsernamePasswordAuthenticationToken actorAuthentication() {
        long version = accounts.find("EMP-001").orElseThrow().credentialsVersion();
        return new UsernamePasswordAuthenticationToken(new AuthenticatedEmployee("EMP-001", version), null,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }
}
