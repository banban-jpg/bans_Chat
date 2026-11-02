package egovframework.example.chat.service;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.chat.domain.DirectConversation;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationAccessPolicyTest {
    @Test
    void onlyActiveParticipantsCanRead() {
        var chat = new DirectConversation("CHAT-1", "EMP-001", "EMP-002");
        var policy = new ConversationAccessPolicy();
        assertThat(policy.canRead(actor("EMP-003", AccountStatus.ACTIVE), chat)).isFalse();
        assertThat(policy.canRead(actor("EMP-001", AccountStatus.ACTIVE), chat)).isTrue();
        assertThat(policy.canRead(actor("EMP-001", AccountStatus.DEPARTED), chat)).isFalse();
    }

    private SessionActor actor(String id, AccountStatus status) {
        return new SessionActor(id, "D01", status, Set.of(SecurityRole.EMPLOYEE));
    }
}
