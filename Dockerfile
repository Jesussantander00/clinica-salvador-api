# ---------- Etapa 1: compilación con Maven ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
# Primero solo el pom para aprovechar la caché de dependencias de Docker
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: imagen liviana de ejecución ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S clinica && adduser -S clinica -G clinica
COPY --from=build /app/target/clinica-salvador-api.jar app.jar
USER clinica
EXPOSE 8080
ENV TZ=America/Bogota
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
