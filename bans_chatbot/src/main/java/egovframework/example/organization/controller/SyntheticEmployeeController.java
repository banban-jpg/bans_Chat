package egovframework.example.organization.controller;

import egovframework.example.common.response.ApiResponse;
import egovframework.example.organization.domain.Employee;
import egovframework.example.organization.service.EmployeeDirectoryService;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Local demonstration API for invented employees; absent from Oracle mode. */
@RestController
@Profile("local-mock")
public class SyntheticEmployeeController {
    private final EmployeeDirectoryService service;

    public SyntheticEmployeeController(EmployeeDirectoryService service) {
        this.service = service;
    }

    @GetMapping("/api/demo/employees")
    public ApiResponse<List<Employee>> employees() {
        return ApiResponse.of(service.syntheticEmployees());
    }
}
