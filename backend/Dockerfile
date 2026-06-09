# Single-stage runtime Dockerfile for pre-built Spring Boot jar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the pre-built jar from local build directory
COPY build/libs/*.jar app.jar

# Expose backend port
EXPOSE 8080

# Run with local profile active
ENV SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java", "-jar", "app.jar"]
