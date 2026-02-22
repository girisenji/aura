package io.github.girisenji.ai.aura.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAI-compatible chat completion response
 */
public record AuraResponse(
    @JsonProperty("id")
    String id,
    
    @JsonProperty("object")
    String object,
    
    @JsonProperty("created")
    long created,
    
    @JsonProperty("model")
    String model,
    
    @JsonProperty("choices")
    List<Choice> choices,
    
    @JsonProperty("usage")
    Usage usage,
    
    @JsonProperty("system_fingerprint")
    String systemFingerprint
) {
    /**
     * Create a non-streaming response
     */
    public static AuraResponse create(String model, String content, Usage usage) {
        Message message = new Message("assistant", content, null);
        Choice choice = new Choice(0, message, "stop");
        
        return new AuraResponse(
            "chatcmpl-" + System.currentTimeMillis(),
            "chat.completion",
            System.currentTimeMillis() / 1000,
            model,
            List.of(choice),
            usage,
            null
        );
    }
    
    /**
     * Create a streaming chunk
     */
    public static AuraResponse streamingChunk(String model, String content, boolean isLast) {
        Message delta = new Message("assistant", content, null);
        Choice choice = Choice.streaming(0, delta, isLast ? "stop" : null);
        
        return new AuraResponse(
            "chatcmpl-" + System.currentTimeMillis(),
            "chat.completion.chunk",
            System.currentTimeMillis() / 1000,
            model,
            List.of(choice),
            null,
            null
        );
    }
}
