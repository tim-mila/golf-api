# Stage 1 — build
FROM eclipse-temurin:25-jdk-alpine@sha256:d3f9f60ad2040582239e2977ee753d598787d8b064ca39a8e131860165dd81fb AS builder
WORKDIR /build

COPY api/mvnw .
COPY api/.mvn .mvn
COPY api/pom.xml .
# Download dependencies first for better layer caching
RUN ./mvnw dependency:go-offline -q

COPY api/src src
RUN ./mvnw package -DskipTests -q

# Extract layered jar for optimized runtime image
RUN java -Djarmode=tools -jar target/golf-api-*.jar extract --layers --launcher --destination target/extracted

# Stage 2 — runtime
FROM eclipse-temurin:25-jre-alpine@sha256:5fcc27581b238efbfda93da3a103f59e0b5691fe522a7ac03fe8057b0819c888 AS runtime

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
