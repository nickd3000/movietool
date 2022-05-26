FROM --platform=linux/arm64 openjdk:11-jdk-slim

RUN mkdir /app
COPY ./target/movietool-1.0.0.jar /app/movietool-1.0.0.jar
WORKDIR /usr/src/project
CMD "java" "-jar" "/app/movietool-1.0.0.jar"