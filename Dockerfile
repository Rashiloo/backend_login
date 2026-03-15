# Dockerfile para despliegue del backend
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/login-backend-*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker

CMD ["java", "-jar", "app.jar"]
