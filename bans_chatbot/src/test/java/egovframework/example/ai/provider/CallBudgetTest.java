package egovframework.example.ai.provider;

import java.time.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CallBudgetTest {
    static class MutableClock extends Clock {
        private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-11-04T07:59:00Z"));
        void advance(Duration duration) { now.updateAndGet(time -> time.plus(duration)); }
        @Override public Instant instant() { return now.get(); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }
    static GeminiSettings settings(int minute, int day, Duration timeout) {
        return new GeminiSettings("gemini", "gemini-2.5-flash-lite", true, true,
                64, 1, minute, day, Duration.ofMillis(100), timeout);
    }
    @Test void countsAttemptsAndResetsMinuteAndPacificDayWithoutSleeping() {
        var clock = new MutableClock();
        var budget = new CallBudget(settings(1, 2, Duration.ofSeconds(1)), clock);
        budget.begin();
        assertThatThrownBy(budget::begin).isInstanceOf(ProviderFailure.class).hasMessage("AI_BUSY");
        budget.finish();
        assertThatThrownBy(budget::begin).hasMessage("AI_QUOTA_EXHAUSTED");
        clock.advance(Duration.ofSeconds(60)); // Midnight Pacific on this date, also the exact sliding window boundary.
        budget.begin(); budget.finish();
        clock.advance(Duration.ofSeconds(60));
        budget.begin(); budget.finish();
        clock.advance(Duration.ofSeconds(60));
        assertThatThrownBy(budget::begin).hasMessage("AI_QUOTA_EXHAUSTED");
        clock.advance(Duration.ofDays(1));
        budget.begin(); budget.finish();
    }
    @Test void upstreamCooldownAlsoBlocksOtherCallers() {
        var clock = new MutableClock(); var budget = new CallBudget(settings(10, 100, Duration.ofSeconds(1)), clock);
        budget.coolDown(5);
        assertThatThrownBy(budget::begin).hasMessage("AI_COOLDOWN");
        clock.advance(Duration.ofSeconds(5)); budget.begin(); budget.finish();
    }
}
