FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app

# Copy the Maven wrapper and pom files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Copy the project source
COPY src ./src/

# Build the application
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built artifact from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"] 