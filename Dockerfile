# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

# Copy entire aems-backend directory structure
COPY aems-backend/pom.xml .
COPY aems-backend/src ./src

# Download dependencies (for layer caching)
RUN mvn dependency:resolve

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /build/target/*.jar app.jar

# Render/Cloud Run requires listening on $PORT (default 8080)
EXPOSE 8080

# Optimized JVM flags for containers:
# - UseContainerSupport: respect container memory limits
# - MaxRAMPercentage: use up to 75% of container RAM
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
