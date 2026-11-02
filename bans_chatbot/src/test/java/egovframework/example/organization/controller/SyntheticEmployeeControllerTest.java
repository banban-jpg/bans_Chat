package egovframework.example.organization.controller;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.service.SessionBoundaryInterceptor;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local-mock")
class SyntheticEmployeeControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void localMockControllerReadsSyntheticFixtureThroughService() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute(SessionBoundaryInterceptor.SESSION_ACTOR,
                new SessionActor("EMP-001", "D01", AccountStatus.ACTIVE, Set.of(SecurityRole.EMPLOYEE)));
        mvc.perform(get("/api/demo/employees").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(20))
                .andExpect(jsonPath("$.data[0].employeeId").value("EMP-001"))
                .andExpect(jsonPath("$.data[0].departmentCode").value("D01"))
                .andExpect(jsonPath("$.data[0].companyEmail").value("emp001@bans.example"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("X-Demo-Data", "SYNTHETIC_EPHEMERAL"));
    }
}
