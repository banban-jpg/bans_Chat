package egovframework.example.organization.controller;

import egovframework.example.auth.domain.AuthenticatedEmployee;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local-mock")
class SyntheticEmployeeControllerTest {
    @Autowired MockMvc mvc;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired PasswordEncoder passwords;

    @Test
    void localMockControllerReadsSyntheticFixtureThroughService() throws Exception {
        accounts.resetForTest();
        accounts.provisionForTest("EMP-001", passwords.encode(java.util.UUID.randomUUID().toString()));
        long version = accounts.find("EMP-001").orElseThrow().credentialsVersion();
        var auth = new UsernamePasswordAuthenticationToken(new AuthenticatedEmployee("EMP-001", version), null,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        mvc.perform(get("/api/demo/employees").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(20))
                .andExpect(jsonPath("$.data[0].employeeId").value("EMP-001"))
                .andExpect(jsonPath("$.data[0].departmentCode").value("D01"))
                .andExpect(jsonPath("$.data[0].companyEmail").value("emp001@bans.example"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("X-Demo-Data", "SYNTHETIC_EPHEMERAL"));
    }
}
