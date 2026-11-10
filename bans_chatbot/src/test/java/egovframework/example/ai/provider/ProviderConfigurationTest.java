package egovframework.example.ai.provider;

import egovframework.example.auth.domain.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;

class ProviderConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(GeminiProviderConfiguration.class);
    static SessionActor actor() {
        return new SessionActor("EMP-001", "D01", AccountStatus.ACTIVE, Set.of(SecurityRole.EMPLOYEE));
    }

    @Test void missingKeyAndAnAmbientKeyBothKeepDefaultOffline() {
        for (String profile : new String[]{"test", "oracle"}) {
            runner.withPropertyValues("spring.profiles.active=" + profile).run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(ChatProvider.class);
                var provider = context.getBean(ChatProvider.class);
                assertThat(provider).isInstanceOf(MockGeminiProvider.class);
                assertThat(provider.chat(actor(), ChatProvider.SyntheticPrompt.HELLO_KO).mock()).isTrue();
            });
        }
        runner.withPropertyValues("GOOGLE_API_KEY=FAKE_IGNORED_VALUE").run(context ->
                assertThat(context.getBean(ChatProvider.class)).isInstanceOf(MockGeminiProvider.class));
    }

    @Test void liveRequiresProfileAcknowledgementsModelAndKey() {
        runner.withPropertyValues("bans.ai.provider=gemini").run(context ->
                assertThat(context).hasFailed());
        for (String profile : new String[]{"test,gemini-live", "oracle-it,gemini-live"}) {
            ready().withPropertyValues("spring.profiles.active=" + profile).run(context -> assertThat(context).hasFailed());
        }
        for (String missing : new String[]{"bans.ai.external-transfer-acknowledged=false", "bans.ai.free-tier-confirmed=false",
                "bans.ai.model=gemini-2.5-flash", "bans.ai.model=gemini-pro", "GOOGLE_API_KEY="}) {
            ready().withPropertyValues(missing).run(context -> assertThat(context).hasFailed());
        }
        ready().run(context -> assertThat(context).hasNotFailed().hasSingleBean(ChatProvider.class));
    }

    @Test void invalidBudgetsAndTimeoutsFailBeforeTransportCreation() {
        for (String value : new String[]{"bans.ai.max-output-tokens=513", "bans.ai.max-concurrent=0",
                "bans.ai.requests-per-minute=0", "bans.ai.requests-per-day=101",
                "bans.ai.connect-timeout=0s", "bans.ai.read-timeout=31s", "bans.ai.provider=vertex"}) {
            runner.withPropertyValues(value).run(context -> assertThat(context).hasFailed());
        }
    }

    @Test void liveRefusesWireDebugThatCouldExposeHeaders() {
        String property = "jdk.httpclient.HttpClient.log";
        String previous = System.getProperty(property);
        try {
            System.setProperty(property, "headers");
            ready().run(context -> assertThat(context).hasFailed());
        } finally {
            if (previous == null) System.clearProperty(property); else System.setProperty(property, previous);
        }
    }

    private ApplicationContextRunner ready() {
        return runner.withPropertyValues("spring.profiles.active=gemini-live", "bans.ai.provider=gemini",
                "bans.ai.model=gemini-2.5-flash-lite", "bans.ai.external-transfer-acknowledged=true",
                "bans.ai.free-tier-confirmed=true", "GOOGLE_API_KEY=FAKE_TEST_ONLY");
    }
}
