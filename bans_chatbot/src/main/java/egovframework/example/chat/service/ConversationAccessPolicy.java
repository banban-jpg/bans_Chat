package egovframework.example.chat.service;

import egovframework.example.auth.domain.SessionActor;
import egovframework.example.chat.domain.DirectConversation;
import org.springframework.stereotype.Component;

@Component
public class ConversationAccessPolicy {
    public boolean canRead(SessionActor actor, DirectConversation conversation) {
        return actor != null && actor.activeEmployee() && conversation != null
                && (actor.employeeId().equals(conversation.firstEmployeeId())
                || actor.employeeId().equals(conversation.secondEmployeeId()));
    }
}
