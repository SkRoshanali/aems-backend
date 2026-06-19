# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy entire aems-backend directory (handles all files including pom.xml, src, etc.)
COPY aems-backend . 

# Download dependencies (for layer caching)
RUN mvn dependency:go-offline -B

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Render/Cloud Run requires listening on $PORT (default 8080)
EXPOSE 8080

# Optimized JVM flags for containers:
# - UseContainerSupport: respect container memory limits
# - MaxRAMPercentage: use up to 75% of container RAM
# - TieredStopAtLevel=4: full JIT for better throughput (containers keep warm)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
