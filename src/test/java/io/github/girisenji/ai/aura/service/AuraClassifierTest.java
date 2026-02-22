package io.github.girisenji.ai.aura.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.girisenji.ai.aura.config.AuraProperties;
import io.github.girisenji.ai.aura.model.AuraRequest;
import io.github.girisenji.ai.aura.model.Message;
import io.github.girisenji.ai.aura.model.RoutingTier;

class AuraClassifierTest {

    private AuraClassifier classifier;
    private AuraProperties properties;

    @BeforeEach
    void setUp() {
        properties = mock(AuraProperties.class);
        AuraProperties.Classifier classifierConfig = new AuraProperties.Classifier();
        classifierConfig.setModelPath("models/classifier.onnx");
        when(properties.getClassifier()).thenReturn(classifierConfig);
        
        classifier = new AuraClassifier(properties);
    }

    @Test
    void testSimplePrompt_ClassifiedAsEco() {
        AuraRequest request = new AuraRequest(
            "gpt-4",
            List.of(new Message("user", "Hello", null)),
            false, null, null, null, null, null, null, null, null
        );
        RoutingTier tier = classifier.classify(request);
        assertEquals(RoutingTier.ECO, tier);
    }

    @Test
    void testMediumPrompt_ClassifiedAsBalanced() {
        AuraRequest request = new AuraRequest(
            "gpt-4",
            List.of(new Message("user", "Can you explain the basics of machine learning and provide a simple explanation?", null)),
            false, null, null, null, null, null, null, null, null
        );
        RoutingTier tier = classifier.classify(request);
        assertEquals(RoutingTier.BALANCED, tier);
    }

    @Test
    void testComplexPrompt_ClassifiedAsPremium() {
        AuraRequest request = new AuraRequest(
            "gpt-4",
            List.of(new Message("user", "Implement a comprehensive analysis of the implications of quantum computing on modern cryptography, "
                + "including detailed mathematical proofs and real-world applications. Explain the Shor's algorithm "
                + "and its impact on RSA encryption in detail. Write code for this complex system.", null)),
            false, null, null, null, null, null, null, null, null
        );
        RoutingTier tier = classifier.classify(request);
        assertEquals(RoutingTier.PREMIUM, tier);
    }

    @Test
    void testCodeGeneration_ClassifiedAsPremium() {
        AuraRequest request = new AuraRequest(
            "gpt-4",
            List.of(new Message("user", "Write a Java class that implements a binary search tree", null)),
            false, null, null, null, null, null, null, null, null
        );
        RoutingTier tier = classifier.classify(request);
        assertEquals(RoutingTier.PREMIUM, tier);
    }

    @Test
    void testAnalysisKeyword_ClassifiedAsBalanced() {
        AuraRequest request = new AuraRequest(
            "gpt-4",
            List.of(new Message("user", "Analyze the pros and cons of microservices", null)),
            false, null, null, null, null, null, null, null, null
        );
        RoutingTier tier = classifier.classify(request);
        assertTrue(tier == RoutingTier.BALANCED || tier == RoutingTier.PREMIUM);
    }
}
