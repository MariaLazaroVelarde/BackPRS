# Multi-stage build para optimizar el tamaño
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage final con JRE más ligero
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/vg-ms-distribution-0.0.1-SNAPSHOT.jar app.jar

# Configuraciones para optimizar el contenedor
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Usuario no-root para seguridad
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup
USER appuser

EXPOSE 8086
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]