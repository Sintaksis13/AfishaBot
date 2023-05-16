FROM gradle:8.1.1-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM amazoncorretto:17.0.7
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/*.jar /app/telegram-bot-app.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/telegram-bot-app.jar"]
