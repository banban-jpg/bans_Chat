package egovframework.example.auth.controller;

import egovframework.example.auth.service.AccountAuthenticationService;
import egovframework.example.auth.service.CurrentSessionService;
import egovframework.example.common.response.ApiResponse;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class ActivationAdminController {
    private final CurrentSessionService sessions;
    private final AccountAuthenticationService authentication;
    public ActivationAdminController(CurrentSessionService sessions, AccountAuthenticationService authentication) {
        this.sessions = sessions; this.authentication = authentication;
    }
    public record Issue(@NotBlank @Pattern(regexp="EMP-[0-9]{3,10}") String employeeCode) { }
    public record Issued(String employeeCode, String code) {
        @Override public String toString() { return "Issued[code=<redacted>]"; }
    }
    @PostMapping("/api/admin/activations")
    public ApiResponse<Issued> issue(@Valid @RequestBody Issue body, HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        String code = authentication.issueActivation(sessions.requireCurrent(request), body.employeeCode());
        return ApiResponse.of(new Issued(body.employeeCode(), code));
    }
}
