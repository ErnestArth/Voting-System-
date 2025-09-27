#
# FROM openjdk:17-jdk-alpine
#
# # Copying the JAR into the container
# COPY target/*.jar /usr/app/app.jar
#
# # Setting working directory
# WORKDIR /usr/app
#
# # Running the JAR
# ENTRYPOINT ["java", "-jar", "app.jar"]

# Build stage
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-alpine
COPY --from=builder /app/target/*.jar /usr/app/app.jar
WORKDIR /usr/app
ENTRYPOINT ["java", "-jar", "app.jar"]

