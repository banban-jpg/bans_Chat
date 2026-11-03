package egovframework.example.global.security;

import egovframework.example.auth.domain.AuthenticatedEmployee;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.auth.service.SessionBoundaryInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Refreshes status, role and credential version on every authenticated HTTP request. */
public class CurrentActorFilter extends OncePerRequestFilter {
    private final AccountRepository accounts;

    public CurrentActorFilter(AccountRepository accounts) { this.accounts = accounts; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedEmployee principal) {
            var current = accounts.find(principal.employeeCode());
            if (current.isEmpty() || !current.get().eligible()
                    || current.get().credentialsVersion() != principal.credentialsVersion()) {
                SecurityContextHolder.clearContext();
                var session = request.getSession(false);
                if (session != null) session.invalidate();
            } else {
                request.setAttribute(SessionBoundaryInterceptor.SESSION_ACTOR, current.get().actor());
            }
        }
        chain.doFilter(request, response);
    }
}
