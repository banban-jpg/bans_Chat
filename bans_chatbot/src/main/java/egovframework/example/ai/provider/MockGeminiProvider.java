package egovframework.example.ai.provider;

import egovframework.example.auth.domain.SessionActor;

/** Has no transport, key or persistence dependency. */
public final class MockGeminiProvider implements ChatProvider {
    public static final String REPLY = "가상 자료에 대한 모의 AI 응답입니다.";
    static void requireAllowed(SessionActor actor, SyntheticPrompt prompt) {
        if (actor == null || !actor.activeEmployee() || prompt == null) {
            throw new ProviderFailure(ProviderFailure.Kind.TRANSFER_DENIED);
        }
    }
    @Override public Result chat(SessionActor actor, SyntheticPrompt prompt) {
        requireAllowed(actor, prompt);
        return new Result(REPLY, true);
    }
    @Override public Result previewTools(egovframework.example.ai.tool.ToolPayload payload) {
        if (payload == null) throw new ProviderFailure(ProviderFailure.Kind.TRANSFER_DENIED);
        payload.outboundJson();
        return new Result(REPLY, true);
    }
    @Override public Result grounded(SessionActor actor, egovframework.example.ai.question.GroundedPrompt prompt) {
        if (actor == null || !actor.activeEmployee() || prompt == null) throw new ProviderFailure(ProviderFailure.Kind.TRANSFER_DENIED);
        try {
            String draft = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(java.util.Map.of("evidenceId", "S1", "quote", prompt.excerpt()));
            return new Result(draft, true);
        } catch (com.fasterxml.jackson.core.JsonProcessingException discarded) { throw new ProviderFailure(ProviderFailure.Kind.INVALID_RESPONSE); }
    }
}
