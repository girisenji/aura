package io.github.girisenji.ai.aura.service.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.girisenji.ai.aura.config.AuraProperties;
import io.github.girisenji.ai.aura.model.AuraRequest;
import io.github.girisenji.ai.aura.model.AuraResponse;
import io.github.girisenji.ai.aura.model.Message;
import io.github.girisenji.ai.aura.model.Usage;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.PostConstruct;

/**
 * OpenAI provider implementation using LangChain4j
 */
@Service
public class OpenAIProvider implements LLMProvider {
    
    private static final Logger log = LoggerFactory.getLogger(OpenAIProvider.class);
    
    private final AuraProperties properties;
    private ChatLanguageModel chatModel;
    private boolean enabled = false;
    
    public OpenAIProvider(AuraProperties properties) {
        this.properties = properties;
    }
    
    @PostConstruct
    public void initialize() {
        String apiKey = properties.getProviders().getOpenai().getApiKey();
        
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("${")) {
            log.warn("OpenAI API key not configured. OpenAI provider disabled.");
            return;
        }
        
        try {
            chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(properties.getProviders().getOpenai().getModels().getBalanced())
                .timeout(properties.getProviders().getOpenai().getTimeout())
                .maxRetries(properties.getProviders().getOpenai().getMaxRetries())
                .logRequests(true)
                .logResponses(true)
                .build();
            
            enabled = true;
            log.info("OpenAI provider initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize OpenAI provider", e);
        }
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public boolean supportsModel(String modelName) {
        return enabled && (modelName.startsWith("gpt-") || modelName.equals("o1"));
    }
    
    @Override
    public AuraResponse generate(AuraRequest request, String modelName) {
        if (!enabled) {
            throw new IllegalStateException("OpenAI provider is not enabled");
        }
        
        try {
            // Convert messages
            List<ChatMessage> messages = convertMessages(request.messages());
            
            // Call OpenAI
            Response<AiMessage> response = chatModel.generate(messages);
            
            // Convert response
            String content = response.content().text();
            Usage usage = new Usage(
                estimateTokens(request.messages()),
                estimateTokens(content),
                estimateTokens(request.messages()) + estimateTokens(content)
            );
            
            return AuraResponse.create(modelName, content, usage);
            
        } catch (Exception e) {
            log.error("Error calling OpenAI", e);
            throw new RuntimeException("Failed to generate response from OpenAI: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void generateStreaming(AuraRequest request, String modelName, Consumer<String> chunkConsumer) {
        if (!enabled) {
            throw new IllegalStateException("OpenAI provider is not enabled");
        }
        
        // TODO: Implement streaming with LangChain4j streaming API
        // For now, fall back to non-streaming
        AuraResponse response = generate(request, modelName);
        String json = serializeResponse(response);
        chunkConsumer.accept(json);
    }
    
    /**
     * Convert Aura messages to LangChain4j messages
     */
    private List<ChatMessage> convertMessages(List<Message> messages) {
        List<ChatMessage> converted = new ArrayList<>();
        
        for (Message msg : messages) {
            switch (msg.role().toLowerCase()) {
                case "system":
                    converted.add(new SystemMessage(msg.content()));
                    break;
                case "user":
                    converted.add(new UserMessage(msg.content()));
                    break;
                case "assistant":
                    converted.add(new AiMessage(msg.content()));
                    break;
                default:
                    log.warn("Unknown message role: {}", msg.role());
            }
        }
        
        return converted;
    }
    
    /**
     * Simple token estimation (rough approximation)
     */
    private int estimateTokens(List<Message> messages) {
        return messages.stream()
            .mapToInt(m -> estimateTokens(m.content()))
            .sum();
    }
    
    private int estimateTokens(String text) {
        // Rough approximation: 1 token ≈ 4 characters
        return text.length() / 4;
    }
    
    /**
     * Serialize response to JSON
     */
    private String serializeResponse(AuraResponse response) {
        // TODO: Use proper JSON serialization
        return String.format(
            "{\"id\":\"%s\",\"object\":\"%s\",\"created\":%d,\"model\":\"%s\"}",
            response.id(), response.object(), response.created(), response.model()
        );
    }
}
