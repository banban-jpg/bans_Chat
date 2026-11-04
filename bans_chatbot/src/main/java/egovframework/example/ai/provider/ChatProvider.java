package egovframework.example.ai.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import egovframework.example.auth.domain.SessionActor;

/** STEP 12 accepts only server-authored greetings. No arbitrary question, history or document input. */
public interface ChatProvider {
    Result chat(SessionActor actor, SyntheticPrompt prompt);

    /** Single reviewed question with one freshly authorized external-allowed synthetic excerpt. */
    default Result grounded(SessionActor actor, egovframework.example.ai.question.GroundedPrompt prompt) {
        throw new ProviderFailure(ProviderFailure.Kind.TRANSFER_DENIED);
    }

    /** Tool context is Mock-only in STEP 13. The live provider deliberately inherits this denial. */
    default Result previewTools(egovframework.example.ai.tool.ToolPayload payload) {
        throw new ProviderFailure(ProviderFailure.Kind.TRANSFER_DENIED);
    }

    enum SyntheticPrompt {
        HELLO_KO("가상 학습용 인사말입니다. 짧게 안녕하세요라고 답해주세요."),
        HELLO_EN("Synthetic learning greeting. Please reply with a short hello.");

        private final String text;
        SyntheticPrompt(String text) { this.text = text; }
        String text() { return text; }
    }

    /** Internal transient result, deliberately not an HTTP conversation DTO. */
    record Result(@JsonIgnore String text, boolean mock) {
        @Override public String toString() { return "ChatResult[mock=" + mock + ", text=<redacted>]"; }
    }
}
