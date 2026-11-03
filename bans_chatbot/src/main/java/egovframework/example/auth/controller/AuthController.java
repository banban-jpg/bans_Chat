package egovframework.example.auth.controller;

import egovframework.example.auth.domain.AuthAccount;
import egovframework.example.auth.domain.AuthenticatedEmployee;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.service.AccountAuthenticationService;
import egovframework.example.auth.service.SessionBoundaryInterceptor;
import egovframework.example.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {
    private final AccountAuthenticationService service;
    private final SecurityContextRepository contexts;
    private final int idleSeconds;

    public AuthController(AccountAuthenticationService service, SecurityContextRepository contexts,
                          @Value("${bans.auth.session-idle-seconds:1800}") int idleSeconds) {
        this.service = service;
        this.contexts = contexts;
        if (idleSeconds < 60 || idleSeconds > 86400) {
            throw new IllegalArgumentException("bans.auth.session-idle-seconds must be 60-86400");
        }
        this.idleSeconds = idleSeconds;
    }

    @GetMapping("/api/auth/csrf")
    public ApiResponse<CsrfView> csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return ApiResponse.of(new CsrfView(token.getHeaderName(), token.getToken()));
    }

    @PostMapping("/api/auth/activate")
    public ApiResponse<ActivationView> activate(@Valid @RequestBody ActivateRequest body) {
        service.activate(body.employeeCode(), body.code(), body.newPassword());
        return ApiResponse.of(new ActivationView(true));
    }

    @PostMapping("/api/auth/login")
    public ApiResponse<MeView> login(@Valid @RequestBody LoginRequest body,
                                     HttpServletRequest request, HttpServletResponse response) {
        AuthAccount account = service.login(body.employeeCode(), body.password());
        request.getSession(true);
        request.changeSessionId();
        request.getSession(false).setMaxInactiveInterval(idleSeconds);
        var principal = new AuthenticatedEmployee(account.employeeCode(), account.credentialsVersion());
        var authorities = account.roles().stream()
                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        return ApiResponse.of(view(account.actor()));
    }

    @PostMapping("/api/auth/logout")
    public ApiResponse<LogoutView> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());
        return ApiResponse.of(new LogoutView(true));
    }

    @GetMapping("/api/auth/me")
    public ApiResponse<MeView> me(HttpServletRequest request) {
        Object value = request.getAttribute(SessionBoundaryInterceptor.SESSION_ACTOR);
        if (!(value instanceof SessionActor actor)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return ApiResponse.of(view(actor));
    }

    private static MeView view(SessionActor actor) {
        return new MeView(actor.employeeId(), actor.departmentCode(), actor.roles());
    }

    public record ActivateRequest(@NotBlank String employeeCode, @NotBlank String code,
                                  @NotBlank @Size(min = 12, max = 128) String newPassword) { }
    public record LoginRequest(@NotBlank String employeeCode, @NotBlank String password) { }
    public record CsrfView(String headerName, String token) { }
    public record ActivationView(boolean activated) { }
    public record LogoutView(boolean loggedOut) { }
    public record MeView(String employeeCode, String departmentCode, Set<SecurityRole> roles) { }
}
