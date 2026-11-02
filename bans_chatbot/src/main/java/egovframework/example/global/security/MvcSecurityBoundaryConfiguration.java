package egovframework.example.global.security;

import egovframework.example.auth.service.SessionBoundaryInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcSecurityBoundaryConfiguration implements WebMvcConfigurer {
    private final SessionBoundaryInterceptor boundary;

    public MvcSecurityBoundaryConfiguration(SessionBoundaryInterceptor boundary) {
        this.boundary = boundary;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(boundary).addPathPatterns("/**");
    }
}
