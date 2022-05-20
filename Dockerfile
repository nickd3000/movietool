FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine

RUN mkdir /app
COPY ./target/movietool-0.0.1.jar /app/movietool-0.0.1.jar
WORKDIR /usr/src/project
CMD "java" "-jar" "movietool-0.0.1.jar"