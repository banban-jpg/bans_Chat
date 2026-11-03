package egovframework.example.chat.websocket;

import egovframework.example.chat.dto.ChatModels.Message;
import egovframework.example.chat.service.ChatService;
import egovframework.example.common.exception.ApiException;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatStompController {
    public record SendFrame(long roomId, String clientRequestId, String content) { }
    public record ReadFrame(long roomId, long lastReadSeq) { }
    public record MessageEvent(long roomId, long roomSeq, String senderEmployeeId,
                               String clientRequestId, String content) { }
    public record Ack(long roomId, long roomSeq, String clientRequestId, long lastReadSeq) { }
    public record ErrorEvent(String code) { }

    private final ChatService chat;
    private final ChatStompSecurity security;
    private final SimpMessagingTemplate messaging;

    public ChatStompController(ChatService chat, ChatStompSecurity security, SimpMessagingTemplate messaging) {
        this.chat = chat;
        this.security = security;
        this.messaging = messaging;
    }

    @MessageMapping("/chat.send")
    public void send(SendFrame payload, SimpMessageHeaderAccessor headers) {
        var actor = security.current(headers);
        var room = chat.room(actor, payload.roomId());
        Message saved = chat.send(actor, payload.roomId(), payload.clientRequestId(), payload.content());
        String other = room.firstEmployeeId().equals(actor.employeeId())
                ? room.secondEmployeeId() : room.firstEmployeeId();
        // ChatService's repository call has returned: its Oracle transaction has committed.
        messaging.convertAndSendToUser(other, "/queue/chat.messages",
                new MessageEvent(payload.roomId(), saved.roomSeq(), actor.employeeId(),
                        payload.clientRequestId(), saved.content()));
        messaging.convertAndSendToUser(actor.employeeId(), "/queue/chat.ack",
                new Ack(payload.roomId(), saved.roomSeq(), payload.clientRequestId(), -1));
    }

    @MessageMapping("/chat.read")
    public void read(ReadFrame payload, SimpMessageHeaderAccessor headers) {
        var actor = security.current(headers);
        long position = chat.read(actor, payload.roomId(), payload.lastReadSeq());
        messaging.convertAndSendToUser(actor.employeeId(), "/queue/chat.ack",
                new Ack(payload.roomId(), -1, null, position));
    }

    @MessageExceptionHandler(ApiException.class)
    public void error(ApiException exception, Principal principal) {
        if (principal != null) {
            messaging.convertAndSendToUser(principal.getName(), "/queue/chat.errors",
                    new ErrorEvent(exception.code()));
        }
    }
}
