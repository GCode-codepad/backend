# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk

LABEL authors="yuqi.guo17@gmail.com"

# Set the working directory in the container
WORKDIR /app

# Copy the jar file to the container
COPY target/user-service-0.0.1-SNAPSHOT.jar /app/app.jar

# Copy the application-docker.properties file to the container
COPY src/main/resources/application-docker.properties /app/application-docker.properties

# Make port 8081 available to the world outside this container
EXPOSE 8081

# Set environment variables to point to the Docker-specific properties file
ENV SPRING_CONFIG_LOCATION=classpath:/application-docker.properties

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
