# Stage 1: Build application
FROM eclipse-temurin:25.0.4_7-jdk-noble AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package -DskipTests


# Stage 2: Run application
FROM eclipse-temurin:25.0.4_7-jre-noble AS runtime

# Install wget for the Docker healthcheck
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*

# Create dedicated system group and non-login user
RUN groupadd --system --gid 10001 appgroup \
    && useradd --system \
        --uid 10001 \
        --gid appgroup \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        appuser

WORKDIR /app

# Copy the application JAR and assign ownership
COPY --from=build \
    --chown=appuser:appgroup \
    /workspace/target/*.jar app.jar

# Ensure /app is owned by the application user
RUN chown appuser:appgroup /app \
    && chmod 755 /app \
    && chmod 644 /app/app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
