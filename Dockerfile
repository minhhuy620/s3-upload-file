FROM eclipse-temurin:11-jdk-focal as build
WORKDIR /build

COPY .mvn/ ./.mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline

COPY . .
RUN sed -i 's/\r$//' mvnw
RUN chmod +x ./mvnw
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:11-jdk-alpine
WORKDIR /app
EXPOSE 8080
COPY --from=build /build/target/*.jar be-1.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/app/be-1.0.1-SNAPSHOT.jar"]



