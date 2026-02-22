package io.github.girisenji.ai.aura.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Configure CORS for OpenAI-compatible clients
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v1/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
    }
    
    /**
     * Configure Jackson for snake_case JSON properties (OpenAI compatibility)
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }
}
