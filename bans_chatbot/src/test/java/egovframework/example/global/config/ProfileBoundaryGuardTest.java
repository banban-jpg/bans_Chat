package egovframework.example.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileBoundaryGuardTest {
    @Test
    void oracleIntegrationRequiresOracleProfile() {
        assertThatThrownBy(() -> check("oracle-it"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oracle-it requires oracle");
    }

    @Test
    void mockAndOracleCannotBeMixed() {
        assertThatThrownBy(() -> check("oracle", "local-mock"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PROFILE_CONFLICT");
    }

    private static void check(String... profiles) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        ProfileBoundaryGuard guard = new ProfileBoundaryGuard();
        guard.setEnvironment(environment);
        guard.postProcessBeanFactory(new DefaultListableBeanFactory());
    }
}
