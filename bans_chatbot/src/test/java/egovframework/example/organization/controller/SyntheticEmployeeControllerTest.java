package egovframework.example.organization.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
        mvc.perform(get("/api/demo/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(20))
                .andExpect(jsonPath("$.data[0].employeeId").value("EMP-001"))
                .andExpect(jsonPath("$.data[0].departmentCode").value("D01"));
    }
}
