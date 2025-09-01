# How to use this dockerfile
# --------------------------
# Build the image, then when running the image as a container, go through the .env.example
# and pass each one as an env variable with -e or adding them to the "environment" section of the service.
# Also you will want to create a volume to the /uploads folder to have access to the uploaded media.

# Stage 1: Build stage
FROM eclipse-temurin:24-jdk AS build

# Set working directory
WORKDIR /app

# Copy the Maven wrapper files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Give execution permission to the Maven wrapper
RUN chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:24-jre

# Set working directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/social_konnect_backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application with environment variables
ENTRYPOINT ["java", "-jar", "/app/app.jar"]