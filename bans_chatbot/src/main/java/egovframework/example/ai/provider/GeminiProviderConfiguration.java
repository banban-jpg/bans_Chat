package egovframework.example.ai.provider;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GeminiSettings.class)
public class GeminiProviderConfiguration {
    @Bean
    public ChatProvider chatProvider(GeminiSettings settings, Environment environment) {
        if ("mock".equals(settings.provider())) return new MockGeminiProvider();
        if (!environment.acceptsProfiles(Profiles.of("gemini-live"))
                || environment.acceptsProfiles(Profiles.of("test", "oracle-it"))) {
            throw new IllegalStateException("AI_LIVE_PROFILE_REQUIRED");
        }
        settings.requireLiveReady();
        // Resolve a key only for explicit live mode; never bind it into a printable configuration record.
        return new GeminiProvider(settings, environment.getProperty("GOOGLE_API_KEY", ""), Clock.systemUTC());
    }
}
