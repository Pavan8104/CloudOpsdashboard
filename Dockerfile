# =======================
# STAGE 1: BUILD
# =======================
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY backend/pom.xml .
RUN mvn dependency:go-offline -q

COPY backend/src ./src

RUN mvn clean package -DskipTests


# =======================
# STAGE 2: RUNTIME (Red Hat Hardened)
# =======================
FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:latest AS runtime

# Red Hat UBI images run as user 185 (jboss) by default for safety
USER 185

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Enterprise-grade JVM settings
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
