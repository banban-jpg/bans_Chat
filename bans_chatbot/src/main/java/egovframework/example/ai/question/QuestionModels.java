package egovframework.example.ai.question;

import egovframework.example.organization.dto.DirectoryModels.EmployeeView;
import java.util.List;

public final class QuestionModels {
    private QuestionModels() { }
    public enum AnswerType { STRUCTURED, DOCUMENT, UNANSWERABLE }
    public record Source(long documentId, long versionId, long fragmentId, String title, String url) { }
    /** Only a server-built answer is public. Never contains the submitted question or raw model output. */
    public record Answer(String answer, AnswerType answerType, List<EmployeeView> structuredFacts, List<Source> sources, String requestId) {
        public Answer { structuredFacts = List.copyOf(structuredFacts); sources = List.copyOf(sources); }
        @Override public String toString() { return "AiAnswer[type=" + answerType + ", sources=" + sources.size() + ", text=<redacted>]"; }
    }
}
