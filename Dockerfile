FROM openjdk:21-jdk-slim

COPY target/order-0.0.1-SNAPSHOT.jar order-0.0.1-SNAPSHOT.jar

EXPOSE 8080

CMD ["java", "-jar", "order-0.0.1-SNAPSHOT.jar"]