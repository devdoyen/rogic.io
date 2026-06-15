# Stage 1: Build stage
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose backend port
EXPOSE 8080

# Run with local profile active
ENV SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java", "-jar", "app.jar"]
