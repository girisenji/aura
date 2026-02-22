package io.github.girisenji.ai.aura.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI auraOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Aura Gateway API")
                .description("Intelligent LLM Gateway with OpenAI-compatible API interface")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Aura Gateway")
                    .url("https://github.com/girisenji/aura"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Local development server"),
                new Server()
                    .url("https://api.aura.example.com")
                    .description("Production server")));
    }
}
