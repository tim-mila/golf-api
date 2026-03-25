# Stage 1 — build
FROM eclipse-temurin:21-jdk-alpine@sha256:c98f0d2e171c898bf896dc4166815d28a56d428e218190a1f35cdc7d82efd61f AS builder
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Download dependencies first for better layer caching
RUN ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw package -DskipTests -q

# Extract layered jar for optimized runtime image
RUN java -Djarmode=tools -jar target/golf-api-*.jar extract --layers --launcher --destination target/extracted

# Stage 2 — runtime
FROM eclipse-temurin:21-jre-alpine@sha256:6ad8ed080d9be96b61438ec3ce99388e294af216ed57356000c06070e85c5d5d AS runtime

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
