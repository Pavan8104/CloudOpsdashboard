# ================================================================
# CloudOps Dashboard - Backend Dockerfile
# Multi-stage build use kar rahe hain - final image chhoti hogi
# Stage 1: Maven se compile karo, Stage 2: Sirf JAR run karo
# GCP Cloud Run ke liye optimized hai yeh image
# ================================================================

# === STAGE 1: BUILD ===
# Maven + JDK 17 - heavy image, sirf build ke liye use hoga
FROM eclipse-temurin:17-jdk-alpine AS builder

LABEL stage="builder"

# Build directory
WORKDIR /app

# Pehle dependencies download karo - layer cache ke liye efficient hai
# Source code change hone pe bhi dependencies wala layer cached rahega
COPY backend/pom.xml .
COPY backend/src ./src

# Maven wrapper bhi copy karo agar use kar rahe ho
# COPY backend/.mvn/ .mvn/
# COPY backend/mvnw .

# Dependencies download karo aur build karo - tests skip karo Docker build mein
# Tests CI/CD pipeline mein alag se chalaate hain
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -q

# === STAGE 2: RUNTIME ===
# JRE only - JDK nahi chahiye runtime pe, image size kam hogi
FROM eclipse-temurin:17-jre-alpine AS runtime

# Security: non-root user se chalao - best practice hai
RUN addgroup -S cloudops && adduser -S cloudops -G cloudops

WORKDIR /app

# Compiled JAR copy karo builder stage se
COPY --from=builder /app/target/*.jar app.jar

# App ko non-root user ke saath chalao
USER cloudops

# GCP Container Registry aur Cloud Run ke liye metadata labels
LABEL maintainer="cloudops-team@google.com"
LABEL version="1.0.0"
LABEL description="CloudOps Dashboard Backend - Spring Boot REST API"

# PORT environment variable - Cloud Run automatically inject karta hai
EXPOSE ${PORT:-8080}

# JVM flags - Cloud Run ke limited memory ke liye optimize karo
# -XX:MaxRAMPercentage=75 matlab available RAM ka 75% heap ke liye
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Health check - Docker aur GCP ke liye
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:${PORT:-8080}/api/actuator/health || exit 1

# Application start karo
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
