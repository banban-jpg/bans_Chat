package egovframework.example.common.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final String publicMessage;

    public ApiException(HttpStatus status, String code, String publicMessage) {
        super(code);
        this.status = status;
        this.code = code;
        this.publicMessage = publicMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String publicMessage() {
        return publicMessage;
    }
}
