# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

LABEL authors="yuqi.guo17@gmail.com"

# Set the working directory inside the container
WORKDIR /app

# Copy the application jar to the container
COPY target/gateway-0.0.1-SNAPSHOT.jar /app/gateway-service.jar

# Copy the application-docker.properties file to the container
COPY src/main/resources/application-docker.properties /app/application-docker.properties

# Expose port 8080
EXPOSE 8080

# Set environment variables to point to the Docker-specific properties file
ENV SPRING_CONFIG_LOCATION=classpath:/application-docker.properties

# Run the application
ENTRYPOINT ["java", "-jar", "gateway-service.jar"]
