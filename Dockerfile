# === Step 1: Build the JAR using Maven (Maven stage) ===
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copy all source code
COPY . ./

# Run Maven build
RUN ./mvnw clean package -DskipTests

# === Step 2: Create final image with only the JAR (Runtime stage) ===
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 5000

# Run the app
CMD ["java", "-jar", "app.jar"]