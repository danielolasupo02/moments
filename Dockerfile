# Use OpenJDK 17
FROM openjdk:17-jdk-slim

# working directory inside the container
WORKDIR /app

# Copy the JAR file from target directory into the container
COPY target/*.jar app.jar

# Command to run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
