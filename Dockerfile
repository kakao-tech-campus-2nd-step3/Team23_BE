FROM amazoncorretto:21

ARG JAR_FILE=build/libs/jeongsan-latest.jar
ARG DB_PROPERTIES=src/main/resources/application-db.properties

COPY ${JAR_FILE} app.jar
COPY ${DB_PROPERTIES} /application-db.properties

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=db", "-Dspring.config.additional-location=file:/application-db.properties", "/app.jar"]
