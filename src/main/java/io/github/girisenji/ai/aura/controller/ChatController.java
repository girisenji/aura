package io.github.girisenji.ai.aura.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.github.girisenji.ai.aura.model.AuraRequest;
import io.github.girisenji.ai.aura.model.AuraResponse;
import io.github.girisenji.ai.aura.model.ErrorResponse;
import io.github.girisenji.ai.aura.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * OpenAI-compatible chat completion endpoint
 */
@RestController
@RequestMapping("/v1")
@Validated
@Tag(name = "Chat Completions", description = "OpenAI-compatible chat completion API")
public class ChatController {
    
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;
    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    /**
     * POST /v1/chat/completions
     * OpenAI-compatible chat completion endpoint
     */
    @Operation(
        summary = "Create chat completion",
        description = "Creates a completion for the chat message. Supports streaming and non-streaming responses."
    )
    @PostMapping("/chat/completions")
    public ResponseEntity<?> chatCompletions(@Valid @RequestBody AuraRequest request) {
        log.info("Received chat completion request - model: {}, stream: {}, messages: {}", 
            request.model(), request.stream(), request.messages().size());
        
        try {
            if (request.stream()) {
                // Return SSE emitter for streaming
                SseEmitter emitter = chatService.streamChatCompletion(request);
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(emitter);
            } else {
                // Return complete response
                AuraResponse response = chatService.chatCompletion(request);
                return ResponseEntity.ok(response);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ErrorResponse.invalidRequest(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.providerError("An error occurred processing your request"));
        }
    }
    
    /**
     * GET /v1/models
     * List available models (OpenAI compatibility)
     */
    @Operation(
        summary = "List models",
        description = "Lists the currently available models and provides basic information about each"
    )
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> listModels() {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("object", "list");
        response.put("data", List.of(
            Map.of("id", "gpt-4o", "object", "model", "owned_by", "openai", "created", System.currentTimeMillis() / 1000),
            Map.of("id", "gpt-4o-mini", "object", "model", "owned_by", "openai", "created", System.currentTimeMillis() / 1000),
            Map.of("id", "gpt-3.5-turbo", "object", "model", "owned_by", "openai", "created", System.currentTimeMillis() / 1000),
            Map.of("id", "claude-3-5-sonnet-20241022", "object", "model", "owned_by", "anthropic", "created", System.currentTimeMillis() / 1000),
            Map.of("id", "claude-3-sonnet-20240229", "object", "model", "owned_by", "anthropic", "created", System.currentTimeMillis() / 1000),
            Map.of("id", "claude-3-haiku-20240307", "object", "model", "owned_by", "anthropic", "created", System.currentTimeMillis() / 1000)
        ));
        return ResponseEntity.ok(response);
    }
    
    /**
     * Exception handler for validation errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.providerError("Internal server error"));
    }
}
