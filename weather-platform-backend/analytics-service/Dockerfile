FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/analytics-service-*.jar app.jar
EXPOSE 8084
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
