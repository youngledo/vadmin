FROM eclipse-temurin:25-jdk AS build

RUN apt-get update \
    && apt-get install --yes --no-install-recommends unzip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
COPY . .
RUN ./mvnw -B -ntp -Pproduction -pl vadmin-reference-app -am package -DskipTests \
    && app_jar="$(find vadmin-reference-app/target -maxdepth 1 -type f \
        -name 'vadmin-reference-app-*.jar' \
        ! -name '*-sources.jar' \
        ! -name '*-javadoc.jar' \
        -print -quit)" \
    && test -n "${app_jar}" \
    && cp "${app_jar}" /workspace/app.jar

FROM eclipse-temurin:25-jre

RUN groupadd --gid 10001 appgroup \
    && useradd --uid 10001 --gid appgroup --create-home --home-dir /app appuser

WORKDIR /app
COPY --from=build --chown=appuser:appgroup /workspace/app.jar /app/app.jar

USER appuser
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
