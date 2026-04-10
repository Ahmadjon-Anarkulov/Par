FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -e -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app
USER app

# spring-boot-maven-plugin создаёт runnable JAR (без суффикса).
# original-*.jar — нерабочий, его не копируем.
COPY --from=build /workspace/target/parlament-bot-1.0.0.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
