FROM openjdk:17-jdk-alpine

COPY build/libs/ShellBot-*-all.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]