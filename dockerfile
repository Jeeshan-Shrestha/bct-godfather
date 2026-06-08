FROM eclipse-temurin:21-jdk

RUN apt-get update && \
    apt-get install -y --no-install-recommends wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN wget -q https://download.documentfoundation.org/libreoffice/stable/26.2.3/deb/x86_64/LibreOffice_26.2.3_Linux_x86-64_deb.tar.gz && \
    tar -xzf LibreOffice_26.2.3_Linux_x86-64_deb.tar.gz && \
    dpkg -i LibreOffice_26.2.3.2_Linux_x86-64_deb/DEBS/*.deb && \
    rm -rf LibreOffice_26.2.3_Linux_x86-64_deb.tar.gz LibreOffice_26.2.3.2_Linux_x86-64_deb

WORKDIR /app
COPY . .

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/EclipseBot-0.0.1-SNAPSHOT.jar"]