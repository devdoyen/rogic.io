# Single-stage runtime Dockerfile for pre-built Spring Boot jar.
# NOTE: Do NOT use multi-stage compilation (gradle builder) inside the container for target EC2 hosts,
# as compiling on resource-constrained hosts (e.g. t3a.nano with 512MB RAM) will trigger OOM and SSH disconnects.
# Always compile the JAR on the host machine (e.g., ./gradlew bootJar) before running Docker builds.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the pre-built jar from local build directory
COPY build/libs/*.jar app.jar

# Expose backend port
EXPOSE 8080

# Run with local profile active
ENV SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java", "-jar", "app.jar"]

