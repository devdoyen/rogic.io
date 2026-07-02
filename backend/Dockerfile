# Single-stage runtime Dockerfile for pre-built Spring Boot jar.
# NOTE: Do NOT use multi-stage compilation (gradle builder) inside the container for target EC2 hosts,
# as compiling on resource-constrained hosts (e.g. t3a.nano with 512MB RAM) will trigger OOM and SSH disconnects.
# Always compile the JAR on the host machine (e.g., ./gradlew bootJar) before running Docker builds.
FROM debian:12-slim
RUN groupadd -r nemogroup && useradd -r -g nemogroup -s /bin/false nemouser
WORKDIR /app

# Copy the pre-compiled native binary
COPY --chown=nemouser:nemogroup build/native/nativeCompile/nemologic-backend app

# Expose backend port
EXPOSE 8080

# Run with local profile active
ENV SPRING_PROFILES_ACTIVE=local
USER nemouser
ENTRYPOINT ["./app"]

