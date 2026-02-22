package io.github.girisenji.ai.aura.service.provider;

import java.util.function.Consumer;

import io.github.girisenji.ai.aura.model.AuraRequest;
import io.github.girisenji.ai.aura.model.AuraResponse;

/**
 * Interface for LLM providers
 */
public interface LLMProvider {
    
    /**
     * Check if this provider is enabled and configured
     */
    boolean isEnabled();
    
    /**
     * Check if this provider supports the given model
     */
    boolean supportsModel(String modelName);
    
    /**
     * Generate a non-streaming response
     */
    AuraResponse generate(AuraRequest request, String modelName);
    
    /**
     * Generate a streaming response
     */
    void generateStreaming(AuraRequest request, String modelName, Consumer<String> chunkConsumer);
}
