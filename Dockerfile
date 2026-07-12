FROM maven:3.9.6-eclipse-temurin-21 AS base

WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn ./.mvn


FROM base AS deps

RUN mvn dependency:go-offline -DskipTests -B


FROM deps AS build

COPY src ./src

RUN mvn package -DskipTests -B


FROM deps AS development

CMD ["mvn", "spring-boot:run"]


FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
