package egovframework.example.audit.repository;

import egovframework.example.audit.domain.AiToolEvent;
import java.util.ArrayDeque;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Bounded ephemeral metadata only. */
@Repository @Profile({"test"})
public class InMemoryAiToolAudit implements AiToolAudit {
    private final ArrayDeque<AiToolEvent> events = new ArrayDeque<>();
    public synchronized void record(AiToolEvent event) {
        if (events.size() == 200) events.removeFirst();
        events.addLast(event);
    }
    public synchronized List<AiToolEvent> eventsForTest() { return List.copyOf(events); }
    public synchronized void resetForTest() { events.clear(); }
}
