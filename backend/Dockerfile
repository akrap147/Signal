FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
ARG MODULE
RUN ./gradlew :${MODULE}:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
ARG MODULE
COPY --from=builder /app/${MODULE}/build/libs/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
