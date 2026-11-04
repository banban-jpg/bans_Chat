package egovframework.example.ai.question;

import egovframework.example.ai.tool.ToolRunBudget;
import egovframework.example.common.exception.ApiException;
import java.time.*;
import org.springframework.http.HttpStatus;

/** Fixed single turn: at most 8 stages, 4 read operations and one bounded provider invocation. */
final class QuestionExecution {
    private final Clock clock;
    private final Instant started;
    private int stages, tools, providerCalls;
    QuestionExecution(Clock clock, Instant started) { this.clock = clock; this.started = started; }
    void stage() { ToolRunBudget.checkTime(started, clock); if (++stages > 8) throw limited(); }
    void tool() { stage(); if (++tools > 4) throw limited(); }
    void provider() { stage(); if (++providerCalls > 1) throw limited(); }
    Duration remaining() { stage(); return Duration.between(clock.instant(), started.plusSeconds(10)); }
    int toolCalls() { return Math.min(tools, 4); }
    int providerCalls() { return Math.min(providerCalls, 1); }
    private ApiException limited() { return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "AI_ORCHESTRATION_LIMIT", "AI orchestration limit reached."); }
}
