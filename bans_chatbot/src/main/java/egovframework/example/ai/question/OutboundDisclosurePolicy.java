package egovframework.example.ai.question;

import egovframework.example.auth.domain.SessionActor;
import egovframework.example.document.dto.DocumentModels.SourceFragment;
import egovframework.example.document.service.DocumentSearchService;
import egovframework.example.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/** Actual question-flow gate: reviewed intent plus fresh domain ACL/version/classification, never model claims. */
final class OutboundDisclosurePolicy {
    private final DocumentSearchService documents;
    OutboundDisclosurePolicy(DocumentSearchService documents) { this.documents=documents; }
    SourceFragment requireAllowed(SessionActor actor, QuestionPolicy.Intent intent, long document, long version, long fragment) {
        if(intent==null||intent.kind()!=QuestionPolicy.Kind.DOCUMENT
                ||new QuestionPolicy().review(intent.canonicalQuestion()).filter(intent::equals).isEmpty())
            throw new ApiException(HttpStatus.FORBIDDEN,"AI_TRANSFER_DENIED","AI request unavailable.");
        return documents.externalSource(actor,document,version,fragment);
    }
}
