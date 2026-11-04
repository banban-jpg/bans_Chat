package egovframework.example.common.web;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Preliminary Oracle shell gate only; not authenticated DB/browser E2E. */
@Tag("oracle-integration")
@EnabledIfEnvironmentVariable(named="ORACLE_UI_TEST",matches="YES")
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles({"oracle","oracle-it"})
class WorkbenchOracleIT {
    @Autowired MockMvc mvc;
    @Test void oracleShellDoesNotClaimEphemeralMockAndDomainApiStillRequiresSession() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Oracle 영속 데이터 모드")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("id=\"demo\""))));
        mvc.perform(get("/ui/app.js")).andExpect(status().isOk());
        mvc.perform(get("/api/chat/rooms")).andExpect(status().isUnauthorized());
    }
}
