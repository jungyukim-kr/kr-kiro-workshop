FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew .
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
