# Stage 1 — build
FROM eclipse-temurin:25-jdk-alpine@sha256:541729c21f9308a68cebbe5a0627e4cd465dfe8980fc03bac0b2feaee57daafd AS builder
WORKDIR /build

COPY api/mvnw .
COPY api/.mvn .mvn
COPY api/pom.xml .
COPY api/checkstyle.xml .
# Download dependencies first for better layer caching
RUN ./mvnw dependency:go-offline -q

COPY api/src src
RUN ./mvnw package -DskipTests -q

# Extract layered jar for optimized runtime image
RUN java -Djarmode=tools -jar target/golf-api-*.jar extract --layers --launcher --destination target/extracted

# Stage 2 — runtime
FROM eclipse-temurin:25-jre-alpine@sha256:2ca9adf44f5c29d28ecd26cf92d75cc0c66b7f32bfd839a4439e363a8b428af8 AS runtime

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
