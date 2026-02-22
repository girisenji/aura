package io.github.girisenji.ai.aura.model;

/**
 * Routing tier for model selection based on prompt complexity
 */
public enum RoutingTier {
    /**
     * Cheapest models for simple queries (GPT-3.5-Turbo, Ollama)
     */
    ECO,
    
    /**
     * Mid-tier models for balanced performance (GPT-4o-mini, Claude-3-Sonnet)
     */
    BALANCED,
    
    /**
     * Premium models for complex tasks (GPT-4o, Claude-3.5-Sonnet)
     */
    PREMIUM
}
