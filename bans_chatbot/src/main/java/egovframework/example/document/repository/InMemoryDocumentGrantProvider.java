package egovframework.example.document.repository;

import egovframework.example.document.domain.DocumentPermission;
import egovframework.example.document.fixture.SyntheticDocumentFixture;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Local-mock/test only; current grants reset with each process and are not Oracle evidence. */
@Repository
@Profile({"test"})
public class InMemoryDocumentGrantProvider implements DocumentGrantProvider {
    private final Set<SyntheticDocumentFixture.SyntheticGrant> grants = SyntheticDocumentFixture.grants();

    @Override
    public boolean hasGrant(String documentId, String employeeId, DocumentPermission permission) {
        return grants.contains(new SyntheticDocumentFixture.SyntheticGrant(documentId, employeeId, permission));
    }
}
