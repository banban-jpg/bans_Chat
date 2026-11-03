package egovframework.example.chat.websocket;

import egovframework.example.auth.domain.AuthenticatedEmployee;
import egovframework.example.auth.repository.AccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration(proxyBeanMethods = false)
@EnableWebSocketMessageBroker
public class ChatWebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    static final String HTTP_SESSION = ChatWebSocketConfiguration.class.getName() + ".session";
    static final String EMPLOYEE = ChatWebSocketConfiguration.class.getName() + ".employee";
    private final ChatStompSecurity security;
    private final AccountRepository accounts;

    public ChatWebSocketConfiguration(ChatStompSecurity security, AccountRepository accounts) {
        this.security = security;
        this.accounts = accounts;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat") // Spring's default handshake Origin check is same-origin.
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler handler, Map<String, Object> attributes) {
                        if (!(request instanceof ServletServerHttpRequest servlet)) return false;
                        HttpServletRequest http = servlet.getServletRequest();
                        HttpSession session = http.getSession(false);
                        var authentication = SecurityContextHolder.getContext().getAuthentication();
                        if (session == null || authentication == null
                                || !(authentication.getPrincipal() instanceof AuthenticatedEmployee employee)) return false;
                        var current = accounts.find(employee.employeeCode());
                        if (current.isEmpty() || !current.get().eligible()
                                || current.get().credentialsVersion() != employee.credentialsVersion()) return false;
                        attributes.put(HTTP_SESSION, session);
                        attributes.put(EMPLOYEE, employee);
                        return true;
                    }
                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler handler, Exception exception) { }
                })
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler handler,
                                                      Map<String, Object> attributes) {
                        var employee = (AuthenticatedEmployee) attributes.get(EMPLOYEE);
                        return employee == null ? null : employee::employeeCode;
                    }
                });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(security);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(security.outbound());
    }
}
