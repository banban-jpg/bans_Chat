package egovframework.example.auth.fixture;

import egovframework.example.auth.domain.SecurityRole;
import egovframework.example.common.fixture.FixtureRows;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Pending synthetic accounts only; no password, activation token, or usable login credential. */
public final class SyntheticAccountFixture {
    private static final List<SyntheticAccount> ACCOUNTS = FixtureRows.read("accounts.psv", 3).stream()
            .map(row -> new SyntheticAccount(row[0], row[1], Arrays.stream(row[2].split(","))
                    .map(SecurityRole::valueOf).collect(Collectors.toUnmodifiableSet())))
            .toList();

    private SyntheticAccountFixture() { }

    public static List<SyntheticAccount> accounts() { return ACCOUNTS; }

    public record SyntheticAccount(String employeeCode, String loginId, Set<SecurityRole> roles) {
        public SyntheticAccount { roles = Set.copyOf(roles); }
    }
}
