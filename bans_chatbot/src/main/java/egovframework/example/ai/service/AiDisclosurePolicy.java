package egovframework.example.ai.service;

import egovframework.example.auth.domain.SessionActor;
import egovframework.example.document.domain.DocumentRecord;
import egovframework.example.document.domain.DocumentSegment;
import egovframework.example.document.service.DocumentAccessPolicy;

/** A second decision after document read authorization, before any network client. */
public class AiDisclosurePolicy {
    private final DocumentAccessPolicy documents;
    private final QuestionSafetyReview questionReview;

    public AiDisclosurePolicy(DocumentAccessPolicy documents, QuestionSafetyReview questionReview) {
        this.documents = documents;
        this.questionReview = questionReview;
    }

    public boolean canDisclose(SessionActor actor, DocumentRecord document, DocumentSegment segment,
                               String question) {
        return document != null && segment != null && question != null && !question.isBlank()
                && documents.canRead(actor, document) && document.approved()
                && document.documentId().equals(segment.documentId())
                && segment.externalAiAllowed() && segment.synthetic() && segment.nonSensitive()
                && questionReview.permitsExternalTransfer(question);
    }
}
