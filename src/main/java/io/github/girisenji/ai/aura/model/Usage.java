package io.github.girisenji.ai.aura.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Token usage information
 */
public record Usage(
    @JsonProperty("prompt_tokens")
    int promptTokens,
    
    @JsonProperty("completion_tokens")
    int completionTokens,
    
    @JsonProperty("total_tokens")
    int totalTokens
) {
    public Usage(int promptTokens, int completionTokens) {
        this(promptTokens, completionTokens, promptTokens + completionTokens);
    }
}
