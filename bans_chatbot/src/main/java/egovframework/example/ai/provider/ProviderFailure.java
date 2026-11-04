package egovframework.example.ai.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import egovframework.example.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/** Never retains the upstream request, response, headers, API key or exception cause. */
public final class ProviderFailure extends ApiException {
    public enum Kind {
        TRANSFER_DENIED(HttpStatus.FORBIDDEN), BUSY(HttpStatus.TOO_MANY_REQUESTS),
        QUOTA_EXHAUSTED(HttpStatus.TOO_MANY_REQUESTS), COOLDOWN(HttpStatus.TOO_MANY_REQUESTS),
        UPSTREAM_FORBIDDEN(HttpStatus.SERVICE_UNAVAILABLE), UPSTREAM_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS),
        UPSTREAM_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE), TIMEOUT(HttpStatus.GATEWAY_TIMEOUT),
        INVALID_RESPONSE(HttpStatus.BAD_GATEWAY);
        private final HttpStatus status;
        Kind(HttpStatus status) { this.status = status; }
    }

    private final Kind kind;
    private final long retryAfterSeconds;
    public ProviderFailure(Kind kind) { this(kind, 0); }
    public ProviderFailure(Kind kind, long retryAfterSeconds) {
        super(kind.status, "AI_" + kind.name(), "AI request unavailable.");
        this.kind = kind;
        this.retryAfterSeconds = retryAfterSeconds;
    }
    public Kind kind() { return kind; }
    @JsonIgnore public long retryAfterSeconds() { return retryAfterSeconds; }
}
