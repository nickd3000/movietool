FROM --platform=linux/arm64 openjdk:11-jdk-slim

RUN mkdir /app
COPY ./target/movietool*.jar /app/movietool.jar
WORKDIR /usr/src/project
CMD "java" "-jar" "/app/movietool.jar"