FROM maven:3.9.12-amazoncorretto-17 AS builder

WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests -B

FROM amazoncorretto:17-alpine

WORKDIR /app

COPY --from=builder /app/target/simple-poc-1.0.0-SNAPSHOT.jar ./app.jar

ENV JAVA_OPTS="-Xms128m -Xmx512m"

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
