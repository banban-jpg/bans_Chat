package egovframework.example.audit.domain;

/** Fixed metadata only: no tool arguments, query, document body, question, answer, exception or key. */
public record AiToolEvent(String actorEmployeeCode, Outcome outcome, int callCount, int itemCount) {
    public enum Outcome { SUCCESS, DENIED, ERROR }
    public AiToolEvent {
        if ((actorEmployeeCode != null && !actorEmployeeCode.matches("EMP-[0-9]{3,10}"))
                || outcome == null || callCount < 0 || callCount > 4 || itemCount < 0 || itemCount > 20) {
            throw new IllegalArgumentException("AI_AUDIT_METADATA_INVALID");
        }
    }
    public String resourceSummary() { return "calls=" + callCount + ";items=" + itemCount; }
}
