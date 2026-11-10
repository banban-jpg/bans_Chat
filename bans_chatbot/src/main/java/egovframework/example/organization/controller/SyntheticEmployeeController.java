package egovframework.example.organization.controller;

import egovframework.example.common.response.ApiResponse;
import egovframework.example.organization.domain.Employee;
import egovframework.example.organization.service.EmployeeDirectoryService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Local demonstration API for invented employees; absent from Oracle mode. */
@RestController
@Profile("fixture-test")
public class SyntheticEmployeeController {
    private final EmployeeDirectoryService service;

    public SyntheticEmployeeController(EmployeeDirectoryService service) {
        this.service = service;
    }

    @GetMapping("/api/demo/employees")
    public ApiResponse<List<Employee>> employees(HttpServletResponse response) {
        response.setHeader("X-Demo-Data", "SYNTHETIC_EPHEMERAL");
        return ApiResponse.of(service.syntheticEmployees());
    }
}
