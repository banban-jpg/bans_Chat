package egovframework.example.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.common.response.ApiError;
import egovframework.example.common.response.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(10); }

    /** Prevent Boot from creating or logging a generated default-user password. */
    @Bean
    public UserDetailsService disabledDefaultUser() {
        return username -> { throw new UsernameNotFoundException("No framework default user"); };
    }

    @Bean
    public Clock authClock() { return Clock.systemUTC(); }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public HttpSessionCsrfTokenRepository sessionCsrfTokens() {
        var tokens = new HttpSessionCsrfTokenRepository();
        tokens.setSessionAttributeName("BANS_CSRF_TOKEN");
        return tokens;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AccountRepository accounts,
                                                   SecurityContextRepository contexts, ObjectMapper json,
                                                   HttpSessionCsrfTokenRepository csrfTokens) throws Exception {
        http.csrf(csrf -> csrf.csrfTokenRepository(csrfTokens)
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));
        http.securityContext(context -> context.securityContextRepository(contexts));
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(fixation -> fixation.changeSessionId()));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/health", "/api/health", "/api/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/activate", "/api/auth/login").permitAll()
                .anyRequest().authenticated());
        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) ->
                        writeError(json, request, response, 401, "AUTH_REQUIRED", "Authentication required."))
                .accessDeniedHandler((request, response, exception) ->
                        writeError(json, request, response, 403, "FORBIDDEN", "Access denied.")));
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.requestCache(AbstractHttpConfigurer::disable);
        http.addFilterBefore(new CurrentActorFilter(accounts), AuthorizationFilter.class);
        return http.build();
    }

    private static void writeError(ObjectMapper json, HttpServletRequest request, HttpServletResponse response,
                                   int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        json.writeValue(response.getOutputStream(), new ApiError(code, message, RequestIdFilter.from(request)));
    }
}
