package io.github.girisenji.ai.aura.service;

import io.github.girisenji.ai.aura.config.AuraProperties;
import io.github.girisenji.ai.aura.model.AuraRequest;
import io.github.girisenji.ai.aura.model.AuraResponse;
import io.github.girisenji.ai.aura.model.RoutingTier;
import io.github.girisenji.ai.aura.model.Usage;
import io.github.girisenji.ai.aura.service.provider.LLMProvider;
import io.github.girisenji.ai.aura.service.provider.OpenAIProvider;
import io.github.girisenji.ai.aura.service.provider.AnthropicProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Dynamic model router with failover support
 * 
 * Routes requests to appropriate LLM providers based on routing tier
 * and implements waterfall failover strategy.
 */
@Service
public class DynamicModelRouter {
    
    private static final Logger log = LoggerFactory.getLogger(DynamicModelRouter.class);
    
    private final AuraProperties properties;
    private final List<LLMProvider> providers;
    
    // Model chains for each tier (ordered by preference)
    private Map<RoutingTier, List<String>> modelChains;
    
    public DynamicModelRouter(
            AuraProperties properties,
            OpenAIProvider openAIProvider,
            AnthropicProvider anthropicProvider) {
        this.properties = properties;
        this.providers = List.of(openAIProvider, anthropicProvider);
    }
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing DynamicModelRouter");
        
        modelChains = new HashMap<>();
        
        // PREMIUM tier: Best models with fallbacks
        String premiumOpenAI = getModelSafe(properties.getProviders().getOpenai().getModels().getPremium(), "gpt-4o");
        String premiumAnthropic = getModelSafe(properties.getProviders().getAnthropic().getModels().getPremium(), "claude-3-5-sonnet-20241022");
        modelChains.put(RoutingTier.PREMIUM, List.of(
            premiumOpenAI,
            premiumAnthropic,
            "gpt-4-turbo"
        ));
        
        // BALANCED tier: Mid-tier models
        String balancedOpenAI = getModelSafe(properties.getProviders().getOpenai().getModels().getBalanced(), "gpt-4o-mini");
        String balancedAnthropic = getModelSafe(properties.getProviders().getAnthropic().getModels().getBalanced(), "claude-3-sonnet-20240229");
        modelChains.put(RoutingTier.BALANCED, List.of(
            balancedOpenAI,
            balancedAnthropic,
            "gemini-pro"
        ));
        
        // ECO tier: Cheapest models
        String ecoOpenAI = getModelSafe(properties.getProviders().getOpenai().getModels().getEco(), "gpt-3.5-turbo");
        String ecoOllama = getModelSafe(properties.getProviders().getOllama().getModels().getDefaultModel(), "llama3");
        modelChains.put(RoutingTier.ECO, List.of(
            ecoOpenAI,
            ecoOllama,
            "mistral-7b"
        ));
        
        log.info("Model chains configured: {}", modelChains);
    }
    
    /**
     * Safely get model name with fallback
     */
    private String getModelSafe(String model, String fallback) {
        return (model != null && !model.isEmpty()) ? model : fallback;
    }
    
    /**
     * Route a non-streaming request with failover
     */
    public AuraResponse route(AuraRequest request, RoutingTier tier) {
        List<String> models = modelChains.get(tier);
        
        for (String modelName : models) {
            try {
                log.info("Attempting to route to model: {}", modelName);
                
                // Find a provider that supports this model
                for (LLMProvider provider : providers) {
                    if (provider.isEnabled() && provider.supportsModel(modelName)) {
                        log.info("Using provider {} for model {}", provider.getClass().getSimpleName(), modelName);
                        return provider.generate(request, modelName);
                    }
                }
                
                log.warn("No enabled provider found for model: {}", modelName);
                
            } catch (Exception e) {
                log.warn("Failed to get response from {}: {}", modelName, e.getMessage());
                // Continue to next model in chain
            }
        }
        
        // Fall back to mock response if all providers failed
        log.warn("All providers failed for tier: {}, returning mock response", tier);
        return createMockResponse(request, models.get(0));
    }
    
    /**
     * Route a streaming request with failover
     */
    public void routeStreaming(AuraRequest request, RoutingTier tier, Consumer<String> chunkConsumer) {
        List<String> models = modelChains.get(tier);
        
        for (String modelName : models) {
            try {
                log.info("Attempting to stream from model: {}", modelName);
                
                // Find a provider that supports this model
                for (LLMProvider provider : providers) {
                    if (provider.isEnabled() && provider.supportsModel(modelName)) {
                        log.info("Using provider {} for streaming model {}", provider.getClass().getSimpleName(), modelName);
                        provider.generateStreaming(request, modelName, chunkConsumer);
                        return;
                    }
                }
                
                log.warn("No enabled provider found for model: {}", modelName);
                
            } catch (Exception e) {
                log.warn("Failed to stream from {}: {}", modelName, e.getMessage());
                // Continue to next model in chain
            }
        }
        
        // Fall back to mock response
        log.warn("All providers failed for tier: {}, using mock streaming", tier);
        mockStreamingResponse(request, models.get(0), chunkConsumer);
    }
    
    /**
     * Create a mock response (fallback when no providers are configured)
     */
    private AuraResponse createMockResponse(AuraRequest request, String model) {
        String content = String.format(
            "This is a mock response from %s. Configure API keys to use real LLM providers.",
            model
        );
        
        Usage usage = new Usage(10, 20);
        return AuraResponse.create(model, content, usage);
    }
    
    /**
     * Mock streaming response (fallback)
     */
    private void mockStreamingResponse(AuraRequest request, String model, Consumer<String> chunkConsumer) {
        String[] words = String.format(
            "This is a mock streaming response from %s. Configure API keys to use real LLM providers.", model
        ).split(" ");
        
        for (String word : words) {
            AuraResponse chunk = AuraResponse.streamingChunk(model, word + " ", false);
            chunkConsumer.accept(serializeChunk(chunk));
            
            // Simulate streaming delay
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Send final chunk
        AuraResponse finalChunk = AuraResponse.streamingChunk(model, "", true);
        chunkConsumer.accept(serializeChunk(finalChunk));
    }
    
    /**
     * Serialize a response chunk to JSON
     */
    private String serializeChunk(AuraResponse chunk) {
        return String.format(
            "{\"id\":\"%s\",\"object\":\"%s\",\"created\":%d,\"model\":\"%s\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"%s\"},\"finish_reason\":%s}]}",
            chunk.id(),
            chunk.object(),
            chunk.created(),
            chunk.model(),
            chunk.choices().get(0).delta() != null ? chunk.choices().get(0).delta().content() : "",
            chunk.choices().get(0).finishReason() != null ? "\"" + chunk.choices().get(0).finishReason() + "\"" : "null"
        );
    }
}
