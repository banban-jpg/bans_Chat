package egovframework.example.audit.domain;

/** No question, answer, source content, tool arguments, error detail or key fields. */
public record AiQuestionEvent(String actorEmployeeCode, Model model, AnswerKind answerKind, Outcome outcome,
                              int toolCalls, int providerCalls, int elapsedMillis) {
    public enum Model { NONE, MOCK, GEMINI_FLASH_LITE }
    public enum AnswerKind { NONE, STRUCTURED, DOCUMENT, UNANSWERABLE }
    public enum Outcome { SUCCESS, DENIED, ERROR }
    public AiQuestionEvent {
        if ((actorEmployeeCode != null && !actorEmployeeCode.matches("EMP-[0-9]{3,10}")) || model == null || answerKind == null || outcome == null
                || toolCalls < 0 || toolCalls > 4 || providerCalls < 0 || providerCalls > 1 || elapsedMillis < 0 || elapsedMillis > 60000) {
            throw new IllegalArgumentException("AI_QUESTION_AUDIT_INVALID");
        }
    }
    public String resourceSummary() { return "m=" + model + ";t=" + toolCalls + ";p=" + providerCalls + ";ms=" + elapsedMillis + ";a=" + answerKind; }
}
