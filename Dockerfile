FROM maven:3.9-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /target/salaahtracker-1.0-SNAPSHOT.jar app.jar
EXPOSE 7070
CMD ["java", "-jar", "app.jar"]