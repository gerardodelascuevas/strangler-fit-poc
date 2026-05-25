FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app

ADD https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/9.4.53.v20231009/jetty-runner-9.4.53.v20231009.jar ./jetty-runner.jar

COPY --from=build /app/target/legacy-catalog.war ./legacy-catalog.war

EXPOSE 8080
CMD ["java", "-jar", "jetty-runner.jar", "--port", "8080", "legacy-catalog.war"]
