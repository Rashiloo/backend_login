# Etapa de construcción
FROM eclipse-temurin:21-jdk-jammy AS build
COPY . .
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre-jammy
COPY --from=build /target/login-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]