# Stage 1: Build
FROM maven:3.9.12-amazoncorretto-17 AS builder

WORKDIR /app

# Copy pom.xml first for layer caching
COPY pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy the Spring Boot jar from builder
COPY --from=builder /app/target/simple-poc-1.0.0-SNAPSHOT.jar ./app.jar

# Set environment variables
ENV JAVA_OPTS="-Xms128m -Xmx512m"

EXPOSE 8080

# Run the Spring Boot application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
