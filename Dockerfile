FROM gradle:8.7-jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle build -x test

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar BudgetAppMs-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]