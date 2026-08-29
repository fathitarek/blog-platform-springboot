FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --create-home --shell /bin/bash spring
COPY --from=build /workspace/target/blog-platform-springboot-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
USER spring
ENTRYPOINT ["java","-jar","/app/app.jar"]
