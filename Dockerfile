FROM openjdk:22-jdk
COPY target/telemetry-1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]