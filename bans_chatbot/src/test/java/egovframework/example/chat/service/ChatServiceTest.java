package egovframework.example.chat.service;

import egovframework.example.auth.domain.*;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.chat.dto.ChatModels.Message;
import egovframework.example.chat.repository.InMemoryChatRepository;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {
    private final InMemoryChatRepository store = new InMemoryChatRepository();
    private final AccountRepository accounts = mock(AccountRepository.class);
    private final ChatService service = new ChatService(store, accounts, new ConversationAccessPolicy());
    private SessionActor actor(String id) {
        return new SessionActor(id, "D01", AccountStatus.ACTIVE, Set.of(SecurityRole.EMPLOYEE));
    }
    @Test void cursorsIdempotencyReadAndAuthorization() {
        var eligible = mock(AuthAccount.class);
        when(eligible.eligible()).thenReturn(true);
        when(accounts.find("EMP-002")).thenReturn(Optional.of(eligible));
        var a = actor("EMP-001"); var b = actor("EMP-002");
        long id = Long.parseLong(service.open(a, "EMP-002").conversationId());
        assertThat(service.open(a, "EMP-002").conversationId()).isEqualTo(Long.toString(id));
        assertThatThrownBy(() -> service.open(a, "EMP-001")).hasMessageContaining("VALIDATION_FAILED");
        assertThatThrownBy(() -> service.messages(actor("EMP-005"), id, 0, 20)).hasMessageContaining("NOT_FOUND");
        for (int i=1;i<=5;i++) service.send(i%2==0 ? b : a, id, "req"+i, "message"+i);
        assertThat(service.send(a, id, "req5", "message5").roomSeq()).isEqualTo(5);
        assertThatThrownBy(() -> service.send(a, id, "req5", "changed")).hasMessageContaining("CHAT_REQUEST_CONFLICT");
        assertThat(service.messages(a,id,0,2).items()).extracting(Message::roomSeq).containsExactly(4L,5L);
        assertThat(service.messages(a,id,4,2).items()).extracting(Message::roomSeq).containsExactly(2L,3L);
        assertThat(service.read(a,id,3)).isEqualTo(3);
        assertThat(service.read(a,id,1)).isEqualTo(3);
        assertThat(service.rooms(a).getFirst().unreadCount()).isEqualTo(1);
        assertThatThrownBy(() -> service.read(a,id,6)).hasMessageContaining("VALIDATION_FAILED");
        assertThatThrownBy(() -> service.send(a,id,"long","x".repeat(2001))).hasMessageContaining("VALIDATION_FAILED");
        assertThat(service.send(a,id,"unicode","😀".repeat(2000)).roomSeq()).isEqualTo(6);
    }
    @Test void twoThreadsReuseRoomAndRequestOnce() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            var start = new CountDownLatch(1);
            Callable<Long> open = () -> {start.await();return Long.parseLong(store.open("EMP-001","EMP-002").conversationId());};
            var x=pool.submit(open);var y=pool.submit(open);start.countDown();long id=x.get();assertThat(y.get()).isEqualTo(id);
            var sendStart=new CountDownLatch(1);
            Callable<Message> send=() -> {sendStart.await();return store.send(id,"EMP-001","same","hello");};
            var first=pool.submit(send);var second=pool.submit(send);sendStart.countDown();
            assertThat(first.get().roomSeq()).isEqualTo(1);assertThat(second.get().roomSeq()).isEqualTo(1);
            assertThat(store.messages(id,0,20).items()).hasSize(1);
        } finally {pool.shutdownNow();}
    }
}

