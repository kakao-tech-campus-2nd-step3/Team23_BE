FROM amazoncorretto:21

ARG JAR_FILE=build/libs/jeongsan-latest.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=db", "/app.jar"]
