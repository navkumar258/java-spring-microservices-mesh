FROM eclipse-temurin:25-noble
WORKDIR /app

RUN groupadd -r appgroup && useradd -r -g appgroup appuser
USER appuser

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]