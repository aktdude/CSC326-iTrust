# syntax=docker/dockerfile:1

FROM eclipse-temurin:11-jdk-focal

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:resolve

COPY src ./src

RUN sed -i "s/localhost/host\.docker\.internal/g" "./src/main/resources/application.yml"

CMD ["./mvnw", "spring-boot:run"]

# docker build --tag itrust2 .
# docker run -d -p 8082:8080 itrust2