FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/campus-safety-0.0.1-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=45s --retries=12 CMD ["sh", "-c", "wget -qO- http://127.0.0.1:8080/actuator/health >/dev/null || exit 1"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
