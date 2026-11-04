package egovframework.example.ai.tool;

import egovframework.example.common.exception.ApiException;
import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import org.springframework.http.HttpStatus;

/** Session-local, bounded counters. No prompts or results are stored in the session. */
public final class ToolRunBudget {
    private static final String KEY = ToolRunBudget.class.getName();
    private final ArrayDeque<Instant> attempts = new ArrayDeque<>();
    private boolean active;
    public static ToolRunBudget of(HttpSession session) {
        synchronized (session) {
            if (!(session.getAttribute(KEY) instanceof ToolRunBudget)) session.setAttribute(KEY, new ToolRunBudget());
            return (ToolRunBudget) session.getAttribute(KEY);
        }
    }
    public synchronized Instant begin(Clock clock) {
        Instant now = clock.instant();
        while (!attempts.isEmpty() && !attempts.peekFirst().isAfter(now.minusSeconds(60))) attempts.removeFirst();
        if (active || attempts.size() >= ToolRegistry.MAX_REQUESTS_PER_MINUTE) throw limited();
        attempts.addLast(now); active = true; return now;
    }
    public static void checkTime(Instant started, Clock clock) {
        if (Duration.between(started, clock.instant()).compareTo(Duration.ofSeconds(10)) >= 0) throw limited();
    }
    public synchronized void finish() { active = false; }
    private static ApiException limited() { return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "AI_TOOL_LIMIT", "Tool request limit reached."); }
}
