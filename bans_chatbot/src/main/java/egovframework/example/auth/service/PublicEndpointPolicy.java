package egovframework.example.auth.service;

import java.util.Set;
import org.springframework.stereotype.Component;

/** Exact anonymous HTTP allowlist for the eventual Spring Security filter chain. */
@Component
public class PublicEndpointPolicy {
    private static final Set<Route> PUBLIC = Set.of(
            new Route("GET", "/health"),
            new Route("GET", "/api/health"),
            new Route("GET", "/api/auth/csrf"),
            new Route("POST", "/api/auth/activate"),
            new Route("POST", "/api/auth/login"));

    public boolean allowsAnonymous(String method, String path) {
        return PUBLIC.contains(new Route(method, path));
    }

    private record Route(String method, String path) { }
}
