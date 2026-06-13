# --- Fase 1: Compilación ---
# Cambiamos de temurin-17 a temurin-21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar el archivo de configuración de dependencias y el código fuente
COPY pom.xml .
COPY src ./src
COPY src/main/resources/geojson/limites_chiclayo.geojson ./src/main/resources/geojson/limites_chiclayo.geojson

# Compilar el proyecto saltando los tests para acelerar el build en el Alfa
RUN mvn clean package -DskipTests

# --- Fase 2: Ejecución ---
# Cambiamos de jre-alpine (17) a jre-alpine (21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear los directorios para subidas de archivos e imágenes si tu app lo requiere
RUN mkdir -p /app/uploads /app/FotosReportes

# Copiar el archivo JAR generado desde la fase de compilación
COPY --from=build /app/target/Basurapp-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto de Spring Boot
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]