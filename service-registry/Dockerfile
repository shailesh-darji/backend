FROM maven:3.8.3-openjdk-17 AS build
COPY src /home/app/service-registry/src
COPY pom.xml /home/app/service-registry
RUN mvn -f /home/app/service-registry/pom.xml clean package
EXPOSE 8761
ENTRYPOINT ["java","-jar","/home/app/service-registry/target/Service-Registry-0.0.1-SNAPSHOT.jar"]

