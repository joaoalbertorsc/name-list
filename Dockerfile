# Stage 1 - build: compile the application into an executable jar using Maven and the JDK
FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /app

# Download dependencies in their own layer, so they are only fetched again when pom.xml changes
COPY pom.xml .
RUN mvn --batch-mode dependency:go-offline

COPY src ./src
RUN mvn --batch-mode package

# Stage 2 - runtime: only a JRE and the jar, without Maven or the source code
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# The port the API listens on inside the container. Publish it with `docker run -p <host-port>:8080`.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
