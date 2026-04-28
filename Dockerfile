# =======================
# STAGE 1: BUILD
# =======================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY backend/pom.xml .
RUN mvn dependency:go-offline -q

COPY backend/src ./src

RUN mvn clean package -DskipTests


# =======================
# STAGE 2: RUNTIME
# =======================
# eclipse-temurin:alpine is the same JDK vendor as the builder stage,
# lightweight, and reliably available on Docker Hub — no registry auth needed.
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# wget ships with busybox in Alpine — no curl needed
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
