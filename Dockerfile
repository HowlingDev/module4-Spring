FROM openjdk:25-ea-25-jdk-slim
WORKDIR /app
COPY target/module4-Spring-0.0.1-SNAPSHOT.jar user-service.jar
ENTRYPOINT ["java", "-jar", "user-service.jar"]
EXPOSE 8080
