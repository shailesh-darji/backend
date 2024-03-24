FROM maven:3.8.3-openjdk-17 AS build
COPY src /home/app/user-service/src
COPY pom.xml /home/app/user-service
RUN mvn -f /home/app/user-service/pom.xml clean package
EXPOSE 8083
ENTRYPOINT ["java","-jar","/home/app/user-service/target/user-service-0.0.1-SNAPSHOT.jar"]
