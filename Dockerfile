FROM maven:3.9.6-eclipse-temurin-21 AS base

WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn ./.mvn


FROM base AS deps

RUN mvn dependency:go-offline -DskipTests -B


FROM deps AS build

COPY src ./src

RUN mvn package -DskipTests -B


FROM deps AS newrelic-agent

ARG NEW_RELIC_AGENT_VERSION=9.4.0

RUN mvn dependency:copy \
    -Dartifact=com.newrelic.agent.java:newrelic-agent:${NEW_RELIC_AGENT_VERSION} \
    -DoutputDirectory=/newrelic \
    -Dmdep.stripVersion=true \
    -B


FROM deps AS development

CMD ["mvn", "spring-boot:run"]


FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar
COPY --from=newrelic-agent /newrelic/newrelic-agent.jar /app/newrelic/newrelic-agent.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/app/docker-entrypoint.sh"]
