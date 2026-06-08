FROM eclipse-temurin:21-jdk

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libreoffice-writer \
        fonts-liberation \
        fonts-dejavu \
        fontconfig && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/EclipseBot-0.0.1-SNAPSHOT.jar"]