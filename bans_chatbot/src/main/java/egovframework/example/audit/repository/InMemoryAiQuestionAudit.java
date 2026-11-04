package egovframework.example.audit.repository;
import egovframework.example.audit.domain.AiQuestionEvent;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
@Repository @Profile({"test", "local-mock"})
public class InMemoryAiQuestionAudit implements AiQuestionAudit {
    private final ArrayDeque<AiQuestionEvent> events = new ArrayDeque<>();
    public synchronized void record(AiQuestionEvent event) { if (events.size() == 200) events.removeFirst(); events.addLast(event); }
    public synchronized List<AiQuestionEvent> eventsForTest() { return List.copyOf(events); }
    public synchronized void resetForTest() { events.clear(); }
}
