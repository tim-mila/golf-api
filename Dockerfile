# Stage 1 — build
FROM eclipse-temurin:25-jdk-alpine@sha256:da683f4f02f9427597d8fa162b73b8222fe08596dcebaf23e4399576ff8b037e AS builder
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Download dependencies first for better layer caching
RUN ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw package -DskipTests -q

# Extract layered jar for optimized runtime image
RUN java -Djarmode=tools -jar target/golf-api-0.0.1-SNAPSHOT.jar extract --layers --launcher --destination target/extracted

# Stage 2 — runtime
FROM eclipse-temurin:25-jre-alpine@sha256:f10d6259d0798c1e12179b6bf3b63cea0d6843f7b09c9f9c9c422c50e44379ec AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

# Copy layers in order (least to most frequently changed)
COPY --from=builder /build/target/extracted/dependencies ./
COPY --from=builder /build/target/extracted/spring-boot-loader ./
COPY --from=builder /build/target/extracted/snapshot-dependencies ./
COPY --from=builder /build/target/extracted/application ./

EXPOSE 8080
EXPOSE 9000

STOPSIGNAL SIGTERM

USER appuser

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]
