FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

copy src ./src

RUN mvn clean package -DskipTests

RUN java -Djarmode=layertools -jar target/*.jar extract

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S MyGroup && adduser -S MyUser -G MyGroup && apk add --no-cache wget

COPY --from=builder /app/dependencies/ ./

COPY --from=builder /app/spring-boot-loader/ ./

COPY --from=builder /app/snapshot-dependencies/ ./

COPY --from=builder /app/application/ ./

RUN chown -R MyUser:MyGroup /app

HEALTHCHECK --retries=3 --interval=30s --time-out=5s --start-period=20s \
  CMD wget --spider --quiet http://localhost:8084/actuator/health || exit 1

EXPOSE 8084

USER MyUser

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]