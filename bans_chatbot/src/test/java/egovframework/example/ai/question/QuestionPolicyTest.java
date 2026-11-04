package egovframework.example.ai.question;

import java.nio.charset.StandardCharsets;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class QuestionPolicyTest {
    @Test void strictJsonLengthsAndUnknownFieldsRejectUnsafeEnvelope() {
        var policy = new QuestionPolicy();
        for (String body : new String[]{"null", "{}", "{} {}", "{\"question\":\"a\",\"question\":\"b\"}", "{\"question\":\"x\",\"tools\":[]}", "{\"question\":1}",
                "{\"question\":\"" + "x".repeat(201) + "\"}", "{\"question\":\"a\\nb\"}"}) {
            assertThatThrownBy(() -> policy.parse(body.getBytes(StandardCharsets.UTF_8))).hasMessage("AI_QUESTION_INPUT_INVALID");
        }
        assertThatThrownBy(() -> policy.parse(new byte[2049])).hasMessage("AI_QUESTION_INPUT_INVALID");
        assertThat(policy.review("개인정보 actual work secret")).isEmpty();
        assertThat(policy.review("가상인사02 연락처").orElseThrow().kind()).isEqualTo(QuestionPolicy.Kind.CONTACT);
    }
    @Test void unreviewedQuestionsCannotMintPromptsAndExecutionCapsAreHardBounds() {
        assertThatThrownBy(() -> new GroundedPrompt(new QuestionPolicy.Intent(QuestionPolicy.Kind.DOCUMENT, "ARBITRARY_TEXT", "x"), "safe", Duration.ofSeconds(1)))
                .hasMessage("AI_QUESTION_INPUT_INVALID");
        var clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC); var stages = new QuestionExecution(clock, Instant.EPOCH);
        for(int i=0;i<8;i++) stages.stage(); assertThatThrownBy(stages::stage).hasMessage("AI_ORCHESTRATION_LIMIT");
        var tools = new QuestionExecution(clock, Instant.EPOCH); for(int i=0;i<4;i++) tools.tool();
        assertThatThrownBy(tools::tool).hasMessage("AI_ORCHESTRATION_LIMIT");
        var provider = new QuestionExecution(clock, Instant.EPOCH); provider.provider();
        assertThatThrownBy(provider::provider).hasMessage("AI_ORCHESTRATION_LIMIT");
    }
}
