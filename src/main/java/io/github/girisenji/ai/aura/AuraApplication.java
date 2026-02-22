package io.github.girisenji.ai.aura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aura Gateway - Intelligent LLM Gateway with OpenAI-compatible API
 * 
 * Features:
 * - Intelligent routing based on prompt complexity
 * - Multi-provider support (OpenAI, Anthropic, Azure, Ollama)
 * - Automatic failover and rate limit handling
 * - Cost optimization
 * - Virtual threads for high concurrency
 */
@SpringBootApplication
public class AuraApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuraApplication.class, args);
    }
}
