package egovframework.example.auth.fixture;

import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.organization.fixture.SyntheticOrganizationFixture;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyntheticAccountFixtureTest {
    @Test
    void twelvePendingAccountsReferenceEmployeesAndHaveNoCredentialFields() {
        var accounts = SyntheticAccountFixture.accounts();
        var employees = SyntheticOrganizationFixture.employees().stream()
                .map(e -> e.employeeId()).collect(Collectors.toSet());
        assertThat(accounts).hasSize(12);
        assertThat(accounts.stream().map(a -> a.employeeCode()).collect(Collectors.toSet())).hasSize(12);
        assertThat(accounts.stream().map(a -> a.loginId()).collect(Collectors.toSet())).hasSize(12);
        assertThat(accounts).allSatisfy(a -> {
            assertThat(employees).contains(a.employeeCode());
            assertThat(a.loginId()).matches("emp\\d{3}@bans\\.example");
            assertThat(a.roles()).contains(SecurityRole.EMPLOYEE);
        });
        assertThat(accounts.stream().flatMap(a -> a.roles().stream()).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder(SecurityRole.values());
        assertThat(SyntheticAccountFixture.SyntheticAccount.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .containsExactly("employeeCode", "loginId", "roles");
    }
}
