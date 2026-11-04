package egovframework.example.ai.provider;

import egovframework.example.common.exception.ApiExceptionHandler;
import egovframework.example.common.response.RequestIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProviderErrorContractTest {
    @RestController static class Probe {
        @GetMapping("/test-only-ai-error") void error() {
            throw new ProviderFailure(ProviderFailure.Kind.UPSTREAM_RATE_LIMITED, 2);
        }
    }
    @Test void publicErrorUsesExistingEnvelopeWithoutUpstreamHeadersOrPayload() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new Probe()).setControllerAdvice(new ApiExceptionHandler())
                .addFilters(new RequestIdFilter()).build();
        var result = mvc.perform(get("/test-only-ai-error")).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("AI_UPSTREAM_RATE_LIMITED"))
                .andExpect(jsonPath("$.message").value("AI request unavailable."))
                .andExpect(jsonPath("$.requestId").isNotEmpty()).andReturn();
        assertThat(result.getResponse().getHeader("x-goog-api-key")).isNull();
        assertThat(result.getResponse().getHeader("Retry-After")).isNull();
        assertThat(new com.fasterxml.jackson.databind.ObjectMapper().readTree(result.getResponse().getContentAsString()).size()).isEqualTo(3);
    }
}
