package egovframework.example.document.fixture;

import egovframework.example.common.fixture.FixtureRows;
import egovframework.example.document.domain.DocumentPermission;
import egovframework.example.document.domain.DocumentRecord;
import egovframework.example.document.domain.DocumentScope;
import egovframework.example.document.domain.DocumentSegment;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Synthetic text and metadata; never use real business content in this resource. */
public final class SyntheticDocumentFixture {
    private static final List<SyntheticDocument> DOCUMENTS = FixtureRows.read("documents.psv", 18).stream()
            .map(row -> new SyntheticDocument(row[0], row[1], row[2], row[3],
                    DocumentScope.valueOf(row[4]), FixtureRows.optional(row[5]), row[6], row[7],
                    row[8], row[9], FixtureRows.optional(row[10]), FixtureRows.optional(row[11]),
                    codes(row[12]), codes(row[13]), yes(row[14]), yes(row[15]),
                    yes(row[16]), yes(row[17]))).toList();

    private SyntheticDocumentFixture() { }

    public static List<SyntheticDocument> documents() { return DOCUMENTS; }

    public static Set<SyntheticGrant> grants() {
        return DOCUMENTS.stream().flatMap(document -> {
            var read = document.readGrants().stream().map(employee ->
                    new SyntheticGrant(document.documentCode(), employee, DocumentPermission.READ));
            var approve = document.approveGrants().stream().map(employee ->
                    new SyntheticGrant(document.documentCode(), employee, DocumentPermission.APPROVE));
            return java.util.stream.Stream.concat(read, approve);
        }).collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> codes(String value) {
        return "-".equals(value) ? Set.of() : Arrays.stream(value.split(","))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean yes(String value) { return "Y".equals(value); }

    public record SyntheticGrant(String documentCode, String employeeCode, DocumentPermission permission) { }

    public record SyntheticDocument(String documentCode, String title, String category,
                                    String ownerCode, DocumentScope scope, String departmentCode,
                                    String documentStatus, String versionState, String format,
                                    String body, String reviewerCode, String reviewDecision,
                                    Set<String> readGrants, Set<String> approveGrants,
                                    boolean aiSearchable, boolean externalAiAllowed,
                                    boolean synthetic, boolean nonSensitive) {
        public SyntheticDocument {
            readGrants = Set.copyOf(readGrants);
            approveGrants = Set.copyOf(approveGrants);
        }

        public DocumentRecord accessRecord() {
            return new DocumentRecord(documentCode, ownerCode, departmentCode, scope,
                    "APPROVED".equals(documentStatus));
        }

        public DocumentSegment segment() {
            return new DocumentSegment("FRAG-" + documentCode, documentCode,
                    externalAiAllowed, synthetic, nonSensitive);
        }
    }
}
