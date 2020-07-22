FROM maven:3.5.2-jdk-11 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM openjdk:11-jdk-alpine
COPY --from=build /usr/src/app/target/*.jar /usr/app/app.jar
ENTRYPOINT ["java","-jar","/usr/app/app.jar"]
