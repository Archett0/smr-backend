FROM eclipse-temurin:21.0.4_7-jre
LABEL authors="yc"
COPY target/CloudGateway-0.0.1-SNAPSHOT.jar cloudgateway.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "cloudgateway.jar"]
