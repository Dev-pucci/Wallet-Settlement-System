FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/wallet-settlement-system-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]