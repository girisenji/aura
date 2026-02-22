package io.github.girisenji.ai.aura.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a single message in the chat conversation
 */
public record Message(
    @NotBlank
    @JsonProperty("role")
    String role,
    
    @NotBlank
    @JsonProperty("content")
    String content,
    
    @JsonProperty("name")
    String name
) {
    public Message {
        // Compact constructor for validation
        if (role != null && !role.matches("system|user|assistant|function")) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
