FROM eclipse-temurin:25-noble
WORKDIR /app

RUN groupadd -r appgroup && \
    useradd -r -g appgroup -m -d /home/appuser appuser && \
    chown -R appuser:appgroup /app /home/appuser

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]