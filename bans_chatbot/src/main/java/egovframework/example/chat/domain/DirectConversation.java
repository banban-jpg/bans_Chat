package egovframework.example.chat.domain;

import java.util.Objects;

public record DirectConversation(String conversationId, String firstEmployeeId, String secondEmployeeId) {
    public DirectConversation {
        Objects.requireNonNull(conversationId);
        Objects.requireNonNull(firstEmployeeId);
        Objects.requireNonNull(secondEmployeeId);
        if (firstEmployeeId.equals(secondEmployeeId)) {
            throw new IllegalArgumentException("A direct conversation requires two employees");
        }
    }
}
