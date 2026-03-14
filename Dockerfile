# Stage 1 — build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Download dependencies first for better layer caching
RUN ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw package -DskipTests -q

# Extract layered jar for optimized runtime image (Spring Boot 3.x compatible)
RUN java -Djarmode=layertools -jar target/golf-api-0.0.1-SNAPSHOT.jar extract --destination target/extracted

# Stage 2 — runtime
# Using Google's Distroless for a truly hardened, secure base image
FROM gcr.io/distroless/java21-debian12:nonroot AS runtime

WORKDIR /app

# The distroless nonroot user runs as uid/gid 65532
# Copy layers in order with correct ownership
COPY --chown=65532:65532 --from=builder /build/target/extracted/dependencies/ ./
COPY --chown=65532:65532 --from=builder /build/target/extracted/spring-boot-loader/ ./
COPY --chown=65532:65532 --from=builder /build/target/extracted/snapshot-dependencies/ ./
COPY --chown=65532:65532 --from=builder /build/target/extracted/application/ ./

EXPOSE 8080
EXPOSE 9000

# Distroless runs as nonroot user automatically

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]