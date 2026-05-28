# build 
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom first (layer-cached until pom changes)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Download dependencies (cached as a separate layer)
RUN ./mvnw dependency:go-offline -q

# Copy source and build, skipping tests (tests run in CI, not in image build)
COPY src ./src
RUN ./mvnw package -Dmaven.test.skip=true -q

# runtime 
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring && mkdir -p /app/logs && chown -R spring:spring /app
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
