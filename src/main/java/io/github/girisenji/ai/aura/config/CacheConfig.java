package io.github.girisenji.ai.aura.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration
 * 
 * By default, uses Caffeine (in-memory cache) which is perfect for:
 * - Single instance deployments
 * - Development and testing
 * - Low operational overhead
 * 
 * For distributed deployments with multiple instances, switch to Redis
 * by setting spring.cache.type=redis in application.yml
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    // Caffeine cache is auto-configured by Spring Boot
    // Custom cache configurations can be added here if needed
}
