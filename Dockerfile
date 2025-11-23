FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY . /app

# Compilar el archivo Java
RUN javac CoherenciaServer.java

EXPOSE 8080

# Ejecutar la clase compilada
CMD ["java", "CoherenciaServer"]
