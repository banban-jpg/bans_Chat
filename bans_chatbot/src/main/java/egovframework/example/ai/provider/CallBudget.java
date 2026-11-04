package egovframework.example.ai.provider;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayDeque;
import static egovframework.example.ai.provider.ProviderFailure.Kind.*;

/** One process/provider budget. Attempts, including failures, consume quota; there is no waiting queue. */
final class CallBudget {
    private final GeminiSettings settings;
    private final Clock clock;
    private final ArrayDeque<Instant> minute = new ArrayDeque<>();
    private LocalDate day;
    private int daily, active;
    private Instant blockedUntil = Instant.MIN;

    CallBudget(GeminiSettings settings, Clock clock) { this.settings = settings; this.clock = clock; }
    synchronized void begin() {
        Instant now = clock.instant();
        if (now.isBefore(blockedUntil)) {
            throw new ProviderFailure(COOLDOWN, Math.max(1, Duration.between(now, blockedUntil).toSeconds()));
        }
        if (active >= settings.maxConcurrent()) throw new ProviderFailure(BUSY);
        while (!minute.isEmpty() && !minute.peekFirst().isAfter(now.minusSeconds(60))) minute.removeFirst();
        LocalDate today = now.atZone(ZoneId.of("America/Los_Angeles")).toLocalDate();
        if (!today.equals(day)) { day = today; daily = 0; }
        if (minute.size() >= settings.requestsPerMinute() || daily >= settings.requestsPerDay()) {
            throw new ProviderFailure(QUOTA_EXHAUSTED);
        }
        minute.addLast(now); daily++; active++;
    }
    synchronized void finish() { active--; }
    synchronized void coolDown(long seconds) {
        Instant next = clock.instant().plusSeconds(seconds);
        if (next.isAfter(blockedUntil)) blockedUntil = next;
    }
}
