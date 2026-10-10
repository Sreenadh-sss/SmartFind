# ==============================================================================
# Multi-Stage Dockerfile for SmartFind Spring Boot Backend (Render Compatible)
# ==============================================================================

# Stage 1: Build using Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/smartfind-backend-1.0.0.jar app.jar

ENV PORT=8083
EXPOSE 8083

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
