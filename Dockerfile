# Multi-stage build for Java 25 with Spring Boot 4.0.3
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /build

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apk add --no-cache maven

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /build/target/aura-gateway-*.jar app.jar

# Create non-root user
RUN addgroup -S aura && adduser -S aura -G aura
USER aura

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run with Virtual Threads enabled
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
