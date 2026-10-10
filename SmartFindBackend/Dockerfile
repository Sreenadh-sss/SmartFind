# ==============================================================================
# Dockerfile for SmartFind Spring Boot Backend
# ==============================================================================

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from the target folder
COPY target/smartfind-backend-1.0.0.jar app.jar

# Expose backend port
EXPOSE 8083

# Run the Spring Boot application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
