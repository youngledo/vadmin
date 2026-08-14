FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace
COPY . .
RUN ./mvnw -B -ntp -Pproduction -pl vadmin-reference-app -am package -DskipTests

FROM eclipse-temurin:25-jre

RUN groupadd --gid 10001 appgroup \
    && useradd --uid 10001 --gid appgroup --create-home --home-dir /app appuser

WORKDIR /app
COPY --from=build --chown=appuser:appgroup \
    /workspace/vadmin-reference-app/target/vadmin-reference-app-0.1.0-SNAPSHOT.jar /app/app.jar

USER appuser
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
