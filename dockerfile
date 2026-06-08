FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && \
    apt-get install -y --no-install-recommends libreoffice-writer && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]