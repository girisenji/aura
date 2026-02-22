# Aura Gateway

**Intelligent LLM Gateway with OpenAI-Compatible API**

High-performance LLM gateway with automatic routing, cost optimization, and multi-provider failover. Built with Java 25 Virtual Threads for massive concurrency.

## Features

- **Intelligent Routing**: Automatically routes requests based on prompt complexity
- **Cost Optimization**: Routes simple queries to cheaper models, complex to premium
- **Automatic Failover**: Seamless fallback across multiple providers
- **OpenAI Compatible**: Drop-in replacement for OpenAI API
- **Streaming Support**: Server-Sent Events for real-time responses
- **High Performance**: Java 25 Virtual Threads handle 10,000+ concurrent requests
- **Production Ready**: Full observability with Prometheus metrics

## Quick Start

```bash
# Prerequisites: Java 25+, Maven 3.9+

# Clone and build
git clone https://github.com/girisenji/aura.git
cd aura

# Configure API keys
export OPENAI_API_KEY="your-key"
export ANTHROPIC_API_KEY="your-key"

# Run
mvn spring-boot:run
```

Access at http://localhost:8080

- **API**: http://localhost:8080/v1/chat/completions
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health**: http://localhost:8080/actuator/health

## Usage

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-4",
    "messages": [{"role": "user", "content": "Explain quantum computing"}]
  }'
```

Compatible with OpenAI SDKs:

```python
import openai
openai.api_base = "http://localhost:8080/v1"
response = openai.ChatCompletion.create(
    model="gpt-4",
    messages=[{"role": "user", "content": "Hello!"}]
)
```

## Architecture

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────────┐
│   Client    │─────▶│  Aura Gateway    │─────▶│  LLM Providers  │
│  (OpenAI    │      │                  │      │  - OpenAI       │
│   SDK)      │◀─────│  - Classifier    │◀─────│  - Anthropic    │
└─────────────┘      │  - Router        │      │  - Azure        │
                     │  - Failover      │      │  - Ollama       │
                     └──────────────────┘      └─────────────────┘
```

**Routing Tiers**:
- **ECO**: Simple queries → gpt-3.5-turbo, llama3
- **BALANCED**: Moderate complexity → gpt-4o-mini, claude-3-sonnet
- **PREMIUM**: Complex tasks → gpt-4o, claude-3.5-sonnet

**How It Works**:
1. Request arrives at the gateway
2. `AuraClassifier` analyzes prompt complexity (keywords, length, structure)
3. `DynamicModelRouter` selects optimal model tier
4. Request routed to primary provider (OpenAI, Anthropic, Azure, or Ollama)
5. If primary fails, automatically fails over to backup provider
6. Response cached to optimize repeated queries

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Runtime | Java 25 | Virtual Threads for concurrency |
| Framework | Spring Boot 4.0.3 | Latest stable release |
| LLM Integration | LangChain4j 0.34.0 | Multi-provider support |
| Cache | Caffeine / Redis | Response caching |
| Metrics | Prometheus | Observability |
| API Docs | SpringDoc OpenAPI | Interactive documentation |

## Docker Deployment

**Single Instance**:
```bash
docker build -t aura-gateway .
docker run -p 8080:8080 \
  -e OPENAI_API_KEY="your-key" \
  aura-gateway
```

**Full Stack** (Gateway + Redis + Prometheus + Grafana):
```bash
# Create .env with API keys
cat > .env << EOF
OPENAI_API_KEY=your-key
ANTHROPIC_API_KEY=your-key
EOF

# Start all services
docker-compose up -d
```

Access:
- Gateway: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

## Configuration

### Environment Variables

```bash
# Required - at least one provider
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...

# Optional providers
AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com/
AZURE_OPENAI_API_KEY=...
AZURE_OPENAI_DEPLOYMENT_NAME=gpt-4

# Server configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Cache configuration
SPRING_CACHE_TYPE=caffeine  # or redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Virtual Threads (enabled by default)
SPRING_THREADS_VIRTUAL_ENABLED=true
```

### Application Configuration

Edit `src/main/resources/application.yml`:

```yaml
aura:
  classifier:
    premium-threshold: 0.7
    balanced-threshold: 0.4
  
  providers:
    openai:
      enabled: true
      models:
        premium: gpt-4o
        balanced: gpt-4o-mini
        eco: gpt-3.5-turbo
      timeout: 60s
    
    anthropic:
      enabled: true
      models:
        premium: claude-3-5-sonnet-20241022
        balanced: claude-3-sonnet-20240229
        eco: claude-3-haiku-20240307
      timeout: 60s
    
    azure:
      enabled: false
      deployment-name: ${AZURE_OPENAI_DEPLOYMENT_NAME:gpt-4}
    
    ollama:
      enabled: false
      base-url: http://localhost:11434
      models:
        eco: llama3

spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=10000,expireAfterWrite=1h
  
  threads:
    virtual:
      enabled: true

logging:
  level:
    io.github.girisenji.ai.aura: INFO
```

### Production Configuration

**Systemd Service** (`/etc/systemd/system/aura.service`):
```ini
[Unit]
Description=Aura Gateway
After=network.target

[Service]
Type=simple
User=aura
WorkingDirectory=/opt/aura
ExecStart=/usr/bin/java -jar aura-gateway.jar
Restart=always
RestartSec=10

Environment="OPENAI_API_KEY=sk-..."
Environment="ANTHROPIC_API_KEY=sk-ant-..."
Environment="SPRING_PROFILES_ACTIVE=prod"

[Install]
WantedBy=multi-user.target
```

Enable: `sudo systemctl enable aura && sudo systemctl start aura`

**Nginx Reverse Proxy** (`/etc/nginx/sites-available/aura`):
```nginx
upstream aura {
    server localhost:8080;
    server localhost:8081;  # Add more instances
    server localhost:8082;
}

server {
    listen 80;
    server_name api.yourdomain.com;
    
    location / {
        proxy_pass http://aura;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        
        # For SSE streaming
        proxy_buffering off;
        proxy_cache off;
    }
}
```

## Monitoring

**Health Checks**:
```bash
curl http://localhost:8080/actuator/health
```

**Metrics** (Prometheus format):
```bash
curl http://localhost:8080/actuator/prometheus
```

**Custom Metrics**:
- `aura_requests_total` - Total requests by tier
- `aura_request_duration_seconds` - Request latency
- `aura_provider_requests_total` - Requests by provider
- `aura_provider_failures_total` - Provider failures
- `aura_cache_hits_total` / `aura_cache_misses_total` - Cache efficiency

**Logging**:
```bash
# View logs
journalctl -u aura -f

# Log levels
export LOGGING_LEVEL_IO_GITHUB_GIRISENJI_AI_AURA=DEBUG
```

## Performance

**Virtual Threads** provide massive concurrency with simple code:
- Handle 10,000+ concurrent LLM requests
- Minimal memory overhead (~1MB per 1000 threads)
- Simple imperative code (no reactive complexity)

**Benchmarks** (single instance, 4 cores, 8GB RAM):
- Throughput: ~3000 requests/sec (gateway overhead only)
- P50 Latency: <50ms (excluding LLM call)
- P99 Latency: <200ms (excluding LLM call)
- Memory: ~512MB base + ~2GB under load

**Cache Performance**:
- Caffeine (local): <1ms lookup, limited to single instance
- Redis (distributed): ~2-5ms lookup, shared across instances

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn verify

# Run specific test
mvn test -Dtest=AuraClassifierTest

# Integration test
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-4",
    "messages": [{"role": "user", "content": "Hello!"}]
  }'
```

## Development

### Project Structure

```
src/main/java/io/github/girisenji/ai/aura/
├── AuraApplication.java           # Main entry point
├── config/
│   ├── CacheConfig.java           # Cache configuration
│   ├── OpenAPIConfig.java         # API documentation
│   └── VirtualThreadConfig.java   # Virtual Threads setup
├── controller/
│   └── ChatController.java        # REST API endpoints
├── model/
│   ├── AuraRequest.java           # Request DTO
│   ├── AuraResponse.java          # Response DTO
│   ├── ClassificationTier.java    # Routing tiers
│   └── Message.java               # Chat message
└── service/
    ├── AuraClassifier.java        # Prompt classification
    ├── ChatService.java           # Main orchestration
    ├── DynamicModelRouter.java    # Model selection
    └── provider/
        ├── LLMProvider.java       # Provider interface
        ├── OpenAIProvider.java    # OpenAI integration
        ├── AnthropicProvider.java # Anthropic integration
        ├── AzureOpenAIProvider.java
        └── OllamaProvider.java    # Local Ollama
```

### Build

```bash
# Clean build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Docker image
docker build -t aura-gateway .

# Run locally
mvn spring-boot:run
```

### Adding a New Provider

1. Implement `LLMProvider` interface
2. Add provider config to `application.yml`
3. Register in `DynamicModelRouter`
4. Add environment variables
5. Add tests

Example:
```java
@Service
public class MyProvider implements LLMProvider {
    @Override
    public String chat(String prompt, String model) {
        // Your implementation
    }
}
```

## Scaling

### Horizontal Scaling

```bash
# Deploy multiple instances
docker run -p 8080:8080 aura-gateway
docker run -p 8081:8080 aura-gateway
docker run -p 8082:8080 aura-gateway

# Use Redis for shared caching
export SPRING_CACHE_TYPE=redis
export SPRING_DATA_REDIS_HOST=redis.example.com
```

Place nginx/HAProxy in front for load balancing.

### Vertical Scaling

Resource recommendations:
- **2 cores, 4GB RAM**: ~1000 concurrent requests
- **4 cores, 8GB RAM**: ~5000 concurrent requests
- **8 cores, 16GB RAM**: ~10000+ concurrent requests

Memory usage scales linearly due to Virtual Threads efficiency.

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: aura-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: aura
  template:
    metadata:
      labels:
        app: aura
    spec:
      containers:
      - name: aura
        image: aura-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: aura-secrets
              key: openai-key
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: aura-service
spec:
  selector:
    app: aura
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

## Troubleshooting

**Application won't start**:
```bash
# Check if port is in use
lsof -ti :8080 | xargs kill -9

# Check Java version
java -version  # Should be 25+

# Check logs
mvn spring-boot:run
# Or
journalctl -u aura -n 100
```

**Provider not working**:
```bash
# Verify API key is set
echo $OPENAI_API_KEY

# Test provider directly
curl https://api.openai.com/v1/chat/completions \
  -H "Authorization: Bearer $OPENAI_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model": "gpt-3.5-turbo", "messages": [{"role": "user", "content": "test"}]}'

# Check provider status in logs
grep -i "provider" /var/log/aura/application.log
```

**High latency**:
- Check if cache is enabled: `curl http://localhost:8080/actuator/metrics/cache.gets`
- Monitor provider latency: `curl http://localhost:8080/actuator/metrics/aura.provider.duration`
- Increase timeout in `application.yml`
- Consider using Redis for distributed caching

**Memory issues**:
```bash
# Increase JVM heap
export JAVA_OPTS="-Xmx4g -Xms2g"
mvn spring-boot:run

# Monitor memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

**Redis connection failed**:
```bash
# Fallback to Caffeine (local cache)
export SPRING_CACHE_TYPE=caffeine

# Or fix Redis connection
ping redis-host
telnet redis-host 6379
```

**Streaming not working**:
- Ensure client supports SSE (Server-Sent Events)
- Check nginx/proxy buffering is disabled: `proxy_buffering off;`
- Verify `stream: true` in request body

## API Reference

### Chat Completions

**POST** `/v1/chat/completions`

Request:
```json
{
  "model": "gpt-4",
  "messages": [
    {"role": "system", "content": "You are a helpful assistant."},
    {"role": "user", "content": "Hello!"}
  ],
  "stream": false,
  "temperature": 0.7,
  "max_tokens": 1000
}
```

Response:
```json
{
  "id": "chatcmpl-123",
  "object": "chat.completion",
  "created": 1677652288,
  "model": "gpt-4o",
  "choices": [{
    "index": 0,
    "message": {
      "role": "assistant",
      "content": "Hello! How can I help you today?"
    },
    "finish_reason": "stop"
  }]
}
```

### List Models

**GET** `/v1/models`

Response:
```json
{
  "object": "list",
  "data": [
    {"id": "gpt-4o", "object": "model", "created": 1686935002},
    {"id": "gpt-4o-mini", "object": "model", "created": 1686935002},
    {"id": "claude-3-5-sonnet-20241022", "object": "model", "created": 1686935002}
  ]
}
```

### Health Check

**GET** `/actuator/health`

Response:
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Add tests for your changes
4. Ensure all tests pass: `mvn test`
5. Commit changes: `git commit -m 'Add amazing feature'`
6. Push to branch: `git push origin feature/amazing-feature`
7. Open Pull Request

## License

Apache License 2.0

## Support

- **Issues**: https://github.com/girisenji/aura/issues
- **API Documentation**: http://localhost:8080/swagger-ui.html

---

**Built with Java 25 Virtual Threads • Spring Boot 4.0.3 • LangChain4j**
