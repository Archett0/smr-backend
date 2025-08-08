FROM eclipse-temurin:21.0.4_7-jre
LABEL authors="zc"
COPY target/UserActionService-0.0.1-SNAPSHOT.jar useractionservice.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "useractionservice.jar"]
