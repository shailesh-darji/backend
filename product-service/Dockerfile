FROM maven:3.8.3-openjdk-17 AS build
COPY src /home/app/product-service/src
COPY pom.xml /home/app/product-service
RUN mvn -f /home/app/product-service/pom.xml clean package
EXPOSE 8080
ENTRYPOINT ["java","-jar","/home/app/product-service/target/Product-Service-0.0.1-SNAPSHOT.jar"]
