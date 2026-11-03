package egovframework.example.chat.mapper;

import egovframework.example.chat.repository.ChatRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

/** Explicit Failsafe only, dedicated migrated and seeded synthetic Oracle schema. */
@Tag("oracle-integration")
@EnabledIfEnvironmentVariable(named="ORACLE_CHAT_TEST",matches="YES")
@SpringBootTest @ActiveProfiles({"oracle","oracle-it"})
class ChatOracleIT {
    @Autowired ChatRepository repository;
    @Test @Transactional void roomSequenceDuplicateCursorAndReadRollback() {
        var room=repository.open("EMP-001","EMP-002");
        long id=Long.parseLong(room.conversationId());
        assertThat(repository.open("EMP-001","EMP-002").conversationId()).isEqualTo(room.conversationId());
        var first=repository.send(id,"EMP-001","it-chat-001","synthetic text");
        assertThat(repository.send(id,"EMP-001","it-chat-001","synthetic text").roomSeq()).isEqualTo(first.roomSeq());
        var second=repository.send(id,"EMP-002","it-chat-002","synthetic reply");
        assertThat(second.roomSeq()).isEqualTo(first.roomSeq()+1);
        assertThat(repository.messages(id,0,20).items()).hasSize(2);
        assertThat(repository.read(id,"EMP-002",first.roomSeq())).isEqualTo(first.roomSeq());
        assertThat(repository.read(id,"EMP-002",0)).isEqualTo(first.roomSeq());
        // Spring test transaction rolls back all rows created here.
    }
}
