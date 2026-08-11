FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY backend ./backend

RUN --mount=type=cache,target=/root/.m2 \
    mvn -f backend/pom.xml -pl iaf-app -am install -Dmaven.test.skip=true \
    && mvn -f backend/pom.xml -pl iaf-app package spring-boot:repackage -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

RUN useradd --system --uid 10001 --create-home jhqms
WORKDIR /app
COPY --from=build /workspace/backend/iaf-app/target/iaf-app-0.1.0-SNAPSHOT.jar /app/jh-qms.jar

USER jhqms
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/jh-qms.jar"]
