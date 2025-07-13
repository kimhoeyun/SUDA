FROM eclipse-temurin:17-jdk-alpine

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} arr.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]