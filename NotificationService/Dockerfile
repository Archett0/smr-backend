FROM eclipse-temurin:21.0.4_7-jre
LABEL authors="yc"
COPY target/NotificationService-0.0.1-SNAPSHOT.jar notificationservice.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "notificationservice.jar"]
