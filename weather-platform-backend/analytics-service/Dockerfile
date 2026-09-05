FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S MyGroup && adduser -S MyUser -G MyGroup && apk add --no-cache wget

COPY target/*.jar app.jar

RUN chown -R MyUser:MyGroup /app

HEALTHCHECK --retries=3 --interval=30s --timeout=5s --start-period=20s \
  CMD wget --spider --quiet http://localhost:8084/actuator/health || exit 1

EXPOSE 8084

USER MyUser

ENTRYPOINT ["java", "-jar", "app.jar"]