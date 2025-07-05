FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies in a separate layer for better caching
RUN mvn dependency:go-offline -B
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add a non-root user to run the application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR from the build stage
COPY --from=build /app/target/order-0.0.1-SNAPSHOT.jar app.jar

# Environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8090
ENV EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/orderdb
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=password
ENV SPRING_RABBITMQ_HOST=rabbitmq
ENV SPRING_RABBITMQ_PORT=5672
ENV SPRING_RABBITMQ_USERNAME=guest
ENV SPRING_RABBITMQ_PASSWORD=guest

# Expose the correct port (from application.yaml)
EXPOSE 8090

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8090/actuator/health || exit 1

# Run the application with specific JVM options
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "app.jar"]
