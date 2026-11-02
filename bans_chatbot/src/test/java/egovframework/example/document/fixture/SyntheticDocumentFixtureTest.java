package egovframework.example.document.fixture;

import egovframework.example.auth.domain.AccountStatus;
import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.document.domain.DocumentScope;
import egovframework.example.document.repository.InMemoryDocumentGrantProvider;
import egovframework.example.document.service.DocumentAccessPolicy;
import egovframework.example.organization.fixture.SyntheticOrganizationFixture;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyntheticDocumentFixtureTest {
    @Test
    void twentyDocumentsCoverCategoriesStatesAndValidReferences() {
        var documents = SyntheticDocumentFixture.documents();
        var employeeCodes = SyntheticOrganizationFixture.employees().stream()
                .map(e -> e.employeeId()).collect(Collectors.toSet());
        var departmentCodes = SyntheticOrganizationFixture.departments().stream()
                .map(d -> d.departmentCode()).collect(Collectors.toSet());
        assertThat(documents).hasSize(20);
        assertThat(documents.stream().map(d -> d.documentCode()).collect(Collectors.toSet())).hasSize(20);
        assertThat(documents.stream().map(d -> d.category()).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder("VACATION", "EXPENSE", "DEPLOYMENT", "EQUIPMENT",
                        "CONTRACT", "ONBOARDING", "SECURITY");
        assertThat(documents.stream().map(d -> d.scope()).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder(DocumentScope.values());
        assertThat(documents).filteredOn(d -> "PUBLISHED".equals(d.versionState())).hasSize(16);
        assertThat(documents).filteredOn(d -> "REJECTED".equals(d.reviewDecision())).hasSize(2);
        assertThat(documents).filteredOn(d -> "DISCARDED".equals(d.versionState())).hasSize(2);
        assertThat(documents).allSatisfy(d -> {
            assertThat(employeeCodes).contains(d.ownerCode());
            if (d.reviewerCode() != null) assertThat(employeeCodes).contains(d.reviewerCode());
            assertThat(employeeCodes).containsAll(d.readGrants()).containsAll(d.approveGrants());
            if (d.scope() == DocumentScope.DEPARTMENT) {
                assertThat(departmentCodes).contains(d.departmentCode());
            } else {
                assertThat(d.departmentCode()).isNull();
            }
            assertThat(d.synthetic()).isTrue();
            assertThat(d.body()).isNotBlank();
            assertThat(d.body()).doesNotContainPattern("(?i)(api[_-]?key|password|token|@(?:gmail|naver)\\.com)");
            if (d.externalAiAllowed()) {
                assertThat(d.documentStatus()).isEqualTo("APPROVED");
                assertThat(d.versionState()).isEqualTo("PUBLISHED");
                assertThat(d.aiSearchable() && d.nonSensitive()).isTrue();
            }
        });
    }

    @Test
    void grantExamplesDenyUnlistedEmployeesAndKeepSecurityDocumentsExternalAiDisabled() {
        var policy = new DocumentAccessPolicy(new InMemoryDocumentGrantProvider());
        var crossDepartment = SyntheticDocumentFixture.documents().stream()
                .filter(d -> d.documentCode().equals("DOC-002")).findFirst().orElseThrow();
        assertThat(policy.canRead(actor("EMP-014", "D04"), crossDepartment.accessRecord())).isTrue();
        assertThat(policy.canRead(actor("EMP-015", "D04"), crossDepartment.accessRecord())).isFalse();
        var security = SyntheticDocumentFixture.documents().stream()
                .filter(d -> d.documentCode().equals("DOC-007")).findFirst().orElseThrow();
        assertThat(policy.canRead(actor("EMP-009", "D03"), security.accessRecord())).isTrue();
        assertThat(policy.canRead(actor("EMP-008", "D03"), security.accessRecord())).isFalse();
        assertThat(security.externalAiAllowed()).isFalse();
        assertThat(security.nonSensitive()).isFalse();
    }

    private SessionActor actor(String employeeCode, String departmentCode) {
        return new SessionActor(employeeCode, departmentCode, AccountStatus.ACTIVE,
                Set.of(SecurityRole.EMPLOYEE));
    }
}
