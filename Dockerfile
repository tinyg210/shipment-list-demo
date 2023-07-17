# Build the application
FROM maven:3.8.2-openjdk-17 AS build

WORKDIR /usr/src/app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src/

RUN mvn package -DskipTests

# Run the application
FROM openjdk:17-alpine

WORKDIR /usr/app

COPY --from=build /usr/src/app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]