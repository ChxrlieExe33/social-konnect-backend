# This Dockerfile kind of works, but not completely.
# You must provide a .env file, but mounting it to /config/.env instead of using --env-file
# Also MAKE SURE the .env file is LF format, NOT CRLF


# Also there is some networking trouble, the best way to solve it is run it in a compose with the database sharing the same network
# and set the database hostname to whatever your database service is.


# Use Java 24 as the base image
FROM eclipse-temurin:24-jdk

# Set working directory
WORKDIR /app

# Copy the Maven wrapper files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Give execution permission to the Maven wrapper
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests

# Set the JAR file path
ENV JAR_FILE=/app/target/social_konnect_backend-0.0.1-SNAPSHOT.jar

# Expose the application port
EXPOSE 8080

# Create a directory for external configuration
RUN mkdir -p /config

# Create an entrypoint script that prioritizes external .env file
RUN echo '#!/bin/sh\n\
# Check for externally mounted .env file first\n\
if [ -f /config/.env ]; then\n\
  echo "Using externally provided .env file"\n\
  ENV_FILE=/config/.env\n\
else\n\
  echo "No external .env file found"\n\
  exit 1\n\
fi\n\
\n\
# Load environment variables from the .env file\n\
if [ -f "$ENV_FILE" ]; then\n\
  while IFS= read -r line || [ -n "$line" ]; do\n\
    # Skip comments and empty lines\n\
    case "$line" in\n\
      ""|\#*) continue ;;\n\
    esac\n\
    # Extract variable name and value\n\
    var_name=$(echo "$line" | cut -d= -f1)\n\
    var_value=$(echo "$line" | cut -d= -f2-)\n\
    # Export the variable\n\
    export "$var_name"="$var_value"\n\
  done < "$ENV_FILE"\n\
fi\n\
\n\
# Run the application\n\
exec java -jar $JAR_FILE "$@"' > /app/entrypoint.sh && chmod +x /app/entrypoint.sh

# Run the application
ENTRYPOINT ["/app/entrypoint.sh"]