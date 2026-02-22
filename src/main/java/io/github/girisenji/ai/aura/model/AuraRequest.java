package io.github.girisenji.ai.aura.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * OpenAI-compatible chat completion request
 */
public record AuraRequest(
    @JsonProperty("model")
    String model,
    
    @NotEmpty
    @Valid
    @JsonProperty("messages")
    List<Message> messages,
    
    @JsonProperty("stream")
    Boolean stream,
    
    @JsonProperty("temperature")
    Double temperature,
    
    @JsonProperty("max_tokens")
    Integer maxTokens,
    
    @JsonProperty("top_p")
    Double topP,
    
    @JsonProperty("frequency_penalty")
    Double frequencyPenalty,
    
    @JsonProperty("presence_penalty")
    Double presencePenalty,
    
    @JsonProperty("stop")
    List<String> stop,
    
    @JsonProperty("user")
    String user,
    
    @JsonProperty("metadata")
    Map<String, Object> metadata
) {
    public AuraRequest {
        // Apply defaults
        if (stream == null) {
            stream = false;
        }
        if (temperature == null) {
            temperature = 1.0;
        }
        if (topP == null) {
            topP = 1.0;
        }
        if (frequencyPenalty == null) {
            frequencyPenalty = 0.0;
        }
        if (presencePenalty == null) {
            presencePenalty = 0.0;
        }
    }
    
    /**
     * Extract the full conversation text for classification
     */
    public String getConversationText() {
        return messages.stream()
            .map(Message::content)
            .reduce("", (a, b) -> a + " " + b)
            .trim();
    }
    
    /**
     * Get the last user message
     */
    public String getLastUserMessage() {
        return messages.stream()
            .filter(m -> "user".equals(m.role()))
            .reduce((first, second) -> second)
            .map(Message::content)
            .orElse("");
    }
}
