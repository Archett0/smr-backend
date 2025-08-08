FROM eclipse-temurin:21.0.4_7-jre
LABEL authors="yc"
COPY target/RecommendationService-0.0.1-SNAPSHOT.jar recommendationservice.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "recommendationservice.jar"]
