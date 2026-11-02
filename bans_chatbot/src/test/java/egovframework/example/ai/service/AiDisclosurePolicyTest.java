package egovframework.example.ai.service;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.document.domain.DocumentRecord;
import egovframework.example.document.domain.DocumentScope;
import egovframework.example.document.domain.DocumentSegment;
import egovframework.example.document.repository.DocumentGrantProvider;
import egovframework.example.document.service.DocumentAccessPolicy;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiDisclosurePolicyTest {
    @Test
    void readableDocumentStillNeedsApprovedSegmentAndQuestion() {
        var documents = new DocumentAccessPolicy(mock(DocumentGrantProvider.class));
        var review = mock(QuestionSafetyReview.class);
        var disclosure = new AiDisclosurePolicy(documents, review);
        var employee = actor(AccountStatus.ACTIVE);
        var document = new DocumentRecord("DOC-1", "EMP-001", "D01", DocumentScope.DEPARTMENT, true);
        when(review.permitsExternalTransfer("synthetic question")).thenReturn(true);
        assertThat(documents.canRead(employee, document)).isTrue();
        assertThat(disclosure.canDisclose(employee, document,
                new DocumentSegment("SEG-1", "DOC-1", false, true, true), "synthetic question")).isFalse();
        var approved = new DocumentSegment("SEG-2", "DOC-1", true, true, true);
        assertThat(disclosure.canDisclose(employee, document, approved, "unreviewed question")).isFalse();
        assertThat(disclosure.canDisclose(employee, document, approved, "synthetic question")).isTrue();
        assertThat(disclosure.canDisclose(employee, document,
                new DocumentSegment("SEG-4", "DOC-1", true, false, true), "synthetic question")).isFalse();
        assertThat(disclosure.canDisclose(employee, document,
                new DocumentSegment("SEG-5", "DOC-1", true, true, false), "synthetic question")).isFalse();
        assertThat(disclosure.canDisclose(employee,
                new DocumentRecord("DOC-1", "EMP-001", "D01", DocumentScope.DEPARTMENT, false),
                approved, "synthetic question")).isFalse();
        assertThat(disclosure.canDisclose(employee, document,
                new DocumentSegment("SEG-3", "DOC-2", true, true, true), "synthetic question")).isFalse();
        assertThat(disclosure.canDisclose(actor(AccountStatus.DEPARTED), document, approved,
                "synthetic question")).isFalse();
    }

    private SessionActor actor(AccountStatus status) {
        return new SessionActor("EMP-001", "D01", status, Set.of(SecurityRole.EMPLOYEE));
    }
}
