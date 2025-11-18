# syntax=docker/dockerfile:1

FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /workspace/painel-macroeconomico
# Copia todo o contexto de build (a pasta painel-macroeconomico quando o context é `.`)
COPY . /workspace/painel-macroeconomico/
RUN mvn -q -DskipTests -B package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/painel-macroeconomico/target/*.jar /app/app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]