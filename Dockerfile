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
# STAGE 2: RUNTIME
# =======================
FROM eclipse-temurin:17-jre AS runtime

RUN apt-get update && \
    apt-get install -y wget && \
    rm -rf /var/lib/apt/lists/*

RUN addgroup --system cloudops && \
    adduser --system --ingroup cloudops cloudops

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

USER cloudops

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
