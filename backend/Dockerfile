# Multi-stage build for Spring Boot 3.3 Backend
# Stage 1: Build stage
FROM gradle:8.7-jdk17-alpine AS builder
WORKDIR /app

# Copy gradle config files for caching
COPY build.gradle /app/
COPY settings.gradle /app/

# Copy source code and build bootJar using preinstalled gradle in base image
COPY src /app/src
RUN gradle bootJar --no-daemon -x test

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose backend port
EXPOSE 8080

# Run with local profile active
ENV SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java", "-jar", "app.jar"]

