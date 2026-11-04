package egovframework.example.ai.service;

/** Trusted server-side review. STEP 13 implements only exact reviewed examples, not arbitrary-text certification. */
public interface QuestionSafetyReview {
    boolean permitsExternalTransfer(String question);
}
