package egovframework.example.chat.websocket;

import egovframework.example.auth.domain.AuthenticatedEmployee;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.repository.AccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

/** Revalidates the HTTP session and the current account on every inbound STOMP frame. */
@Component
public class ChatStompSecurity implements ChannelInterceptor {
    private final AccountRepository accounts;
    private final ConcurrentHashMap<String, Object> liveSessions = new ConcurrentHashMap<>();
    public ChatStompSecurity(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public SessionActor current(SimpMessageHeaderAccessor frame) {
        var attributes = frame.getSessionAttributes();
        if (attributes == null) throw new IllegalArgumentException("CHAT_SESSION_REQUIRED");
        var session = attributes.get(ChatWebSocketConfiguration.HTTP_SESSION);
        var employee = attributes.get(ChatWebSocketConfiguration.EMPLOYEE);
        if (!(session instanceof HttpSession http) || !(employee instanceof AuthenticatedEmployee identity))
            throw new IllegalArgumentException("CHAT_SESSION_REQUIRED");
        try {
            var stored = http.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            if (!(stored instanceof SecurityContext context)
                    || context.getAuthentication() == null
                    || !identity.equals(context.getAuthentication().getPrincipal()))
                throw new IllegalArgumentException("CHAT_SESSION_EXPIRED");
            var current = accounts.find(identity.employeeCode());
            if (current.isEmpty() || !current.get().eligible()
                    || current.get().credentialsVersion() != identity.credentialsVersion())
                throw new IllegalArgumentException("CHAT_ACCOUNT_INACTIVE");
            return current.get().actor();
        } catch (IllegalStateException invalidatedSession) {
            throw new IllegalArgumentException("CHAT_SESSION_EXPIRED");
        }
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var frame = StompHeaderAccessor.wrap(message);
        StompCommand command = frame.getCommand();
        if (command == StompCommand.DISCONNECT) {
            if (frame.getSessionId() != null) liveSessions.remove(frame.getSessionId());
            return message;
        }
        if (command == null) return message;
        var actor = current(frame);
        if (command == StompCommand.CONNECT) {
            var session = (HttpSession) frame.getSessionAttributes().get(ChatWebSocketConfiguration.HTTP_SESSION);
            var token = session.getAttribute("BANS_CSRF_TOKEN");
            if (!(token instanceof CsrfToken csrf)
                    || !csrf.getToken().equals(frame.getFirstNativeHeader(csrf.getHeaderName())))
                throw new IllegalArgumentException("CHAT_CSRF_REQUIRED");
            if (frame.getSessionId() != null) liveSessions.put(frame.getSessionId(), frame.getSessionAttributes());
        } else if (command == StompCommand.SEND) {
            var dest = frame.getDestination();
            if (!"/app/chat.send".equals(dest) && !"/app/chat.read".equals(dest))
                throw new IllegalArgumentException("CHAT_DESTINATION_FORBIDDEN");
        } else if (command == StompCommand.SUBSCRIBE) {
            var dest = frame.getDestination();
            if (!"/user/queue/chat.messages".equals(dest)
                    && !"/user/queue/chat.ack".equals(dest)
                    && !"/user/queue/chat.errors".equals(dest))
                throw new IllegalArgumentException("CHAT_DESTINATION_FORBIDDEN");
        } else if (command != StompCommand.UNSUBSCRIBE) {
            throw new IllegalArgumentException("CHAT_COMMAND_FORBIDDEN");
        }
        return message;
    }

    /** The broker must not deliver to a socket after its HTTP session or account is revoked. */
    public ChannelInterceptor outbound() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                var frame = StompHeaderAccessor.wrap(message);
                if (frame.getCommand() != StompCommand.MESSAGE) return message;
                if (frame.getSessionId() == null) return null;
                var attributes = liveSessions.get(frame.getSessionId());
                if (!(attributes instanceof java.util.Map<?, ?> map)) return null;
                var session = map.get(ChatWebSocketConfiguration.HTTP_SESSION);
                var employee = map.get(ChatWebSocketConfiguration.EMPLOYEE);
                if (!(session instanceof HttpSession http) || !(employee instanceof AuthenticatedEmployee identity)) return null;
                try {
                    var context = http.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                    if (!(context instanceof SecurityContext security)
                            || security.getAuthentication() == null
                            || !identity.equals(security.getAuthentication().getPrincipal())) return null;
                    var current = accounts.find(identity.employeeCode());
                    if (current.isEmpty() || !current.get().eligible()
                            || current.get().credentialsVersion() != identity.credentialsVersion()) return null;
                    return message;
                } catch (IllegalStateException invalidatedSession) {
                    return null;
                }
            }
        };
    }
}
