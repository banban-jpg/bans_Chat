package egovframework.example.auth.service;

import egovframework.example.auth.domain.SessionActor;
import egovframework.example.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Early MVC boundary until the Spring Security session chain is wired. */
@Component
public class SessionBoundaryInterceptor implements HandlerInterceptor {
    public static final String SESSION_ACTOR = SessionBoundaryInterceptor.class.getName() + ".actor";
    private final PublicEndpointPolicy publicEndpoints;

    public SessionBoundaryInterceptor(PublicEndpointPolicy publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (publicEndpoints.allowsAnonymous(request.getMethod(), path)) return true;
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute(SESSION_ACTOR);
        if (!(value instanceof SessionActor actor)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Authentication required.");
        }
        if (!actor.activeEmployee()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied.");
        }
        request.setAttribute(SESSION_ACTOR, actor);
        return true;
    }
}
