package io.github.girisenji.ai.aura.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single choice in the response
 */
public record Choice(
    @JsonProperty("index")
    int index,
    
    @JsonProperty("message")
    Message message,
    
    @JsonProperty("finish_reason")
    String finishReason,
    
    @JsonProperty("delta")
    Message delta
) {
    /**
     * Constructor for non-streaming responses
     */
    public Choice(int index, Message message, String finishReason) {
        this(index, message, finishReason, null);
    }
    
    /**
     * Constructor for streaming responses
     */
    public static Choice streaming(int index, Message delta, String finishReason) {
        return new Choice(index, null, finishReason, delta);
    }
}
