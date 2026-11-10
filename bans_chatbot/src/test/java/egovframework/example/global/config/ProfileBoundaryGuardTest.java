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
        assertThatThrownBy(() -> check("oracle", "test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PROFILE_CONFLICT");
    }

    @Test
    void bootstrapRequiresOracleAndCannotBeAnIntegrationTest() {
        assertThatThrownBy(() -> check("oracle-bootstrap"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oracle-bootstrap requires oracle");
        assertThatThrownBy(() -> check("oracle", "oracle-bootstrap", "oracle-it"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oracle-bootstrap requires oracle without oracle-it");
    }

    private static void check(String... profiles) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        ProfileBoundaryGuard guard = new ProfileBoundaryGuard();
        guard.setEnvironment(environment);
        guard.postProcessBeanFactory(new DefaultListableBeanFactory());
    }
}
