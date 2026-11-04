package egovframework.example.ai.provider;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** No API key, URL, arbitrary headers, tools or prompt templates are bindable here. */
@ConfigurationProperties("bans.ai")
public record GeminiSettings(
        @DefaultValue("mock") String provider,
        @DefaultValue("") String model,
        @DefaultValue("false") boolean externalTransferAcknowledged,
        @DefaultValue("false") boolean freeTierConfirmed,
        @DefaultValue("256") int maxOutputTokens,
        @DefaultValue("1") int maxConcurrent,
        @DefaultValue("2") int requestsPerMinute,
        @DefaultValue("20") int requestsPerDay,
        @DefaultValue("3s") Duration connectTimeout,
        @DefaultValue("10s") Duration readTimeout) {
    public GeminiSettings {
        if (!("mock".equals(provider) || "gemini".equals(provider))
                || model == null || maxOutputTokens < 1 || maxOutputTokens > 512
                || maxConcurrent < 1 || maxConcurrent > 2
                || requestsPerMinute < 1 || requestsPerMinute > 10
                || requestsPerDay < 1 || requestsPerDay > 100
                || connectTimeout == null || connectTimeout.compareTo(Duration.ofMillis(100)) < 0
                || connectTimeout.compareTo(Duration.ofSeconds(10)) > 0
                || readTimeout == null || readTimeout.compareTo(Duration.ofMillis(100)) < 0
                || readTimeout.compareTo(Duration.ofSeconds(30)) > 0) {
            throw new IllegalArgumentException("AI_CONFIG_INVALID");
        }
    }

    void requireLiveReady() {
        // Flash is intentionally excluded: Google announced automatic model routing on 2026-12-01.
        if (!"gemini".equals(provider) || !"gemini-2.5-flash-lite".equals(model)
                || !externalTransferAcknowledged || !freeTierConfirmed) {
            throw new IllegalStateException("AI_EXTERNAL_ACTIVATION_REQUIRED");
        }
    }
}
