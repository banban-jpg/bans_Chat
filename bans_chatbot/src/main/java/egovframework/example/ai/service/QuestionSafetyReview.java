package egovframework.example.ai.service;

/** A trusted server-side review. No production implementation or real transport exists yet. */
public interface QuestionSafetyReview {
    boolean permitsExternalTransfer(String question);
}
