package io.github.girisenji.ai.aura.service;

import io.github.girisenji.ai.aura.model.AuraRequest;
import io.github.girisenji.ai.aura.model.AuraResponse;
import io.github.girisenji.ai.aura.model.RoutingTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main service for handling chat completions
 */
@Service
public class ChatService {
    
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    
    private final AuraClassifier classifier;
    private final DynamicModelRouter router;
    
    // Virtual thread executor for async operations
    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
    
    public ChatService(AuraClassifier classifier, DynamicModelRouter router) {
        this.classifier = classifier;
        this.router = router;
    }
    
    /**
     * Process a non-streaming chat completion request
     */
    public AuraResponse chatCompletion(AuraRequest request) {
        log.debug("Processing non-streaming request");
        
        // Step 1: Classify the prompt
        RoutingTier tier = classifier.classify(request);
        log.info("Classified request as tier: {}", tier);
        
        // Step 2: Route to appropriate model
        AuraResponse response = router.route(request, tier);
        
        return response;
    }
    
    /**
     * Process a streaming chat completion request
     */
    public SseEmitter streamChatCompletion(AuraRequest request) {
        log.debug("Processing streaming request");
        
        SseEmitter emitter = new SseEmitter(60_000L); // 60 second timeout
        
        // Handle completion and errors
        emitter.onCompletion(() -> log.debug("SSE completed"));
        emitter.onTimeout(() -> {
            log.warn("SSE timeout");
            emitter.complete();
        });
        emitter.onError(e -> {
            log.error("SSE error", e);
            emitter.completeWithError(e);
        });
        
        // Process streaming in virtual thread
        virtualExecutor.submit(() -> {
            try {
                // Step 1: Classify the prompt
                RoutingTier tier = classifier.classify(request);
                log.info("Classified streaming request as tier: {}", tier);
                
                // Step 2: Stream from appropriate model
                router.routeStreaming(request, tier, chunk -> {
                    try {
                        emitter.send(SseEmitter.event()
                            .data(chunk)
                            .name("message"));
                    } catch (IOException e) {
                        log.error("Error sending SSE chunk", e);
                        throw new RuntimeException(e);
                    }
                });
                
                // Send [DONE] marker (OpenAI compatibility)
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
                
            } catch (Exception e) {
                log.error("Error in streaming", e);
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }
}
