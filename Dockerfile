# Build the Vaadin production bundle and the Spring Boot executable JAR.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src/ src/
COPY package.json package-lock.json vite.config.ts tsconfig.json ./
RUN mvn -B -Pproduction -DskipTests package

# The runtime image contains only the JRE and the assembled application.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd --system --create-home --uid 10001 citacloud

COPY --from=build /workspace/target/*.jar app.jar
RUN mkdir /app/uploads && chown -R citacloud:citacloud /app

USER citacloud
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
