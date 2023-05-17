FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY ./build/libs/HeadsNHands-1.0-standalone.jar ./app.jar

EXPOSE 8080

CMD ["java", "-jar", "./app.jar"]