package egovframework.example.ai.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Map;

/** Server-minted, transient single-fragment capability. No caller IDs, tools, history or titles. */
public final class GroundedPrompt {
    public static final int MAX_OUTPUT_TOKENS = 256, MAX_EXCERPT = 200;
    private final String question, excerpt;
    private final Duration timeout;
    GroundedPrompt(QuestionPolicy.Intent intent, String excerpt, Duration timeout) {
        var reviewed = new QuestionPolicy().review(intent.canonicalQuestion());
        if (reviewed.isEmpty() || reviewed.get().kind() != QuestionPolicy.Kind.DOCUMENT
                || !reviewed.get().equals(intent) || excerpt == null || excerpt.isBlank()
                || excerpt.codePointCount(0, excerpt.length()) > MAX_EXCERPT
                || timeout == null || timeout.isNegative() || timeout.isZero() || timeout.compareTo(Duration.ofSeconds(10)) > 0) {
            throw QuestionPolicy.invalid();
        }
        this.question = intent.canonicalQuestion(); this.excerpt = excerpt; this.timeout = timeout;
    }
    @JsonIgnore public String question() { return question; }
    @JsonIgnore public String excerpt() { return excerpt; }
    @JsonIgnore public Duration timeout() { return timeout; }
    public JsonNode outboundJson() {
        return new ObjectMapper().valueToTree(Map.of("question", question, "evidenceId", "S1", "excerpt", excerpt));
    }
    @Override public String toString() { return "GroundedPrompt[text=<redacted>, fragments=1]"; }
}
