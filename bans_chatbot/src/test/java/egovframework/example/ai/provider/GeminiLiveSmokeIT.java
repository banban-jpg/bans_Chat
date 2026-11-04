package egovframework.example.ai.provider;

import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Opt-in only. Exactly one synthetic greeting, no Spring/Oracle, raw output or automatic retries. */
@Tag("gemini-live")
@EnabledIfSystemProperty(named = "bans.gemini.smoke", matches = "YES")
@EnabledIfEnvironmentVariable(named = "BANS_GEMINI_SMOKE", matches = "YES")
class GeminiLiveSmokeIT {
    @Test void oneExplicitlyAuthorizedSyntheticGreeting() {
        Assumptions.assumeTrue("YES".equals(System.getenv("BANS_GEMINI_FREE_TIER_CONFIRMED")), "Free-tier review required");
        Assumptions.assumeTrue("YES".equals(System.getenv("BANS_GEMINI_TRANSFER_ACKNOWLEDGED")), "External transfer acknowledgement required");
        String key = System.getenv("GOOGLE_API_KEY");
        Assumptions.assumeTrue(key != null && !key.isBlank(), "Environment key required");
        var settings = new GeminiSettings("gemini", "gemini-2.5-flash-lite", true, true, 64, 1, 1, 1,
                Duration.ofSeconds(3), Duration.ofSeconds(10));
        try (var provider = new GeminiProvider(settings, key, Clock.systemUTC())) {
            var result = provider.chat(ProviderConfigurationTest.actor(), ChatProvider.SyntheticPrompt.HELLO_KO);
            assertTrue(!result.mock() && result.text() != null && !result.text().isBlank(), "Synthetic greeting failed");
        }
    }
}
