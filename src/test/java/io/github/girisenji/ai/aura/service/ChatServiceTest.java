package io.github.girisenji.ai.aura.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.girisenji.ai.aura.model.AuraRequest;
import io.github.girisenji.ai.aura.model.AuraResponse;
import io.github.girisenji.ai.aura.model.Message;
import io.github.girisenji.ai.aura.model.RoutingTier;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AuraClassifier classifier;

    @Mock
    private DynamicModelRouter router;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(classifier, router);
    }

    @Test
    void testChatCompletion_Success() {
        // Arrange
        AuraRequest request = new AuraRequest(
            "gpt-4",
            List.of(new Message("user", "Hello", null)),
            false,
            null, null, null, null, null, null, null, null
        );

        when(classifier.classify(any(AuraRequest.class))).thenReturn(RoutingTier.ECO);
        
        AuraResponse mockResponse = new AuraResponse(
            "chatcmpl-123",
            "chat.completion",
            System.currentTimeMillis() / 1000,
            "gpt-3.5-turbo",
            List.of(),
            null,
            null
        );
        when(router.route(any(AuraRequest.class), any(RoutingTier.class))).thenReturn(mockResponse);

        // Act
        AuraResponse response = chatService.chatCompletion(request);

        // Assert
        assertNotNull(response);
        assertEquals("chatcmpl-123", response.id());
        verify(classifier, times(1)).classify(any(AuraRequest.class));
        verify(router, times(1)).route(any(AuraRequest.class), any(RoutingTier.class));
    }

    @Test
    void testChatCompletion_WithMetadata() {
        // Arrange
        AuraRequest request = new AuraRequest(
            "gpt-4",
            List.of(new Message("user", "Complex query", null)),
            false,
            null, null, null, null, null, null, null, 
            java.util.Map.of("user_id", "test-user")
        );

        when(classifier.classify(any(AuraRequest.class))).thenReturn(RoutingTier.PREMIUM);
        when(router.route(any(AuraRequest.class), any(RoutingTier.class))).thenReturn(
            new AuraResponse("id", "chat.completion", 0L, "gpt-4", List.of(), null, null)
        );

        // Act
        AuraResponse response = chatService.chatCompletion(request);

        // Assert
        assertNotNull(response);
        verify(classifier).classify(any(AuraRequest.class));
    }
}
