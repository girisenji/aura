package io.github.girisenji.ai.aura.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Error response for OpenAI compatibility
 */
public record ErrorResponse(
    @JsonProperty("error")
    ErrorDetail error
) {
    public record ErrorDetail(
        @JsonProperty("message")
        String message,
        
        @JsonProperty("type")
        String type,
        
        @JsonProperty("code")
        String code
    ) {}
    
    public static ErrorResponse create(String message, String type, String code) {
        return new ErrorResponse(new ErrorDetail(message, type, code));
    }
    
    public static ErrorResponse invalidRequest(String message) {
        return create(message, "invalid_request_error", null);
    }
    
    public static ErrorResponse rateLimitError(String message) {
        return create(message, "rate_limit_error", "rate_limit_exceeded");
    }
    
    public static ErrorResponse providerError(String message) {
        return create(message, "api_error", "provider_error");
    }
}
