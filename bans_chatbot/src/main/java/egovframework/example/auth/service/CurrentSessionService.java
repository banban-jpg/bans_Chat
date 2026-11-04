package egovframework.example.auth.service;

import egovframework.example.auth.domain.AuthenticatedEmployee;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

/** Revalidates the actual server session for each tool invocation, including calls within one request. */
@Service
public class CurrentSessionService {
    private final AccountRepository accounts;
    public CurrentSessionService(AccountRepository accounts) { this.accounts = accounts; }

    public SessionActor requireCurrent(HttpServletRequest request) {
        var session = request.getSession(false);
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (session == null || authentication == null || !authentication.isAuthenticated()
                    || !(authentication.getPrincipal() instanceof AuthenticatedEmployee identity)) throw denied();
            var stored = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            if (!(stored instanceof SecurityContext context) || context.getAuthentication() == null
                    || !context.getAuthentication().isAuthenticated()
                    || !identity.equals(context.getAuthentication().getPrincipal())) throw denied();
            var account = accounts.find(identity.employeeCode());
            if (account.isEmpty() || !account.get().eligible()
                    || account.get().credentialsVersion() != identity.credentialsVersion()) {
                SecurityContextHolder.clearContext(); session.invalidate(); throw denied();
            }
            return account.get().actor();
        } catch (IllegalStateException expired) { throw denied(); }
    }
    private ApiException denied() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Authentication required.");
    }
}
