FROM eclipse-temurin:21.0.4_7-jre
LABEL authors="yc"
COPY target/UserService-0.0.1-SNAPSHOT.jar userservice.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "userservice.jar"]
