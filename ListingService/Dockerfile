FROM eclipse-temurin:21.0.4_7-jre
LABEL authors="yc"
COPY target/ListingService-0.0.1-SNAPSHOT.jar listingservice.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "listingservice.jar"]
