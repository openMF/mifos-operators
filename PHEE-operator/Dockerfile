# Use an official openjdk runtime as a parent image
FROM openjdk:21-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file
COPY target/ph-ee-operator-1.0.0.jar /app/ph-ee-operator-1.0.0.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/ph-ee-operator-1.0.0.jar"]
