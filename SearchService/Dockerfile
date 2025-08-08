FROM eclipse-temurin:21.0.4_7-jre
LABEL authors="yc"
COPY target/SearchService-0.0.1-SNAPSHOT.jar searchservice.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "searchservice.jar"]
