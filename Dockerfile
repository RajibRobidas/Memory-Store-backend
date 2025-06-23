# Use an official OpenJDK 21 base image
FROM eclipse-temurin:21-jdk

# Set working directory in the container
WORKDIR /app

# Copy everything from backend to the container
COPY . .

# Give permission to Maven Wrapper to execute
RUN chmod +x mvnw

# Build the Spring Boot application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Expose the port (Render provides PORT env variable at runtime)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/memoir-box-0.0.1-SNAPSHOT.jar"]
