# Stage 1 — build
FROM eclipse-temurin:25-jdk-alpine@sha256:c7c3d0bdf7f2bbc5a05a6712ed334d198360105634ea9adf76fca94f381b1e3f AS builder
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
FROM eclipse-temurin:25-jre-alpine@sha256:e1898fd6a11fc965a994d8ace186fd3585aeb636cf8f05f47a723fcfee0b0918 AS runtime

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
