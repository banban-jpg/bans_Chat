package egovframework.example.common.response;

public record ApiError(String code, String message, String requestId) {
}
