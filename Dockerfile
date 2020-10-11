FROM openjdk:11-jre-slim-buster
EXPOSE 8080

ADD app.jar /app.jar

CMD java -jar /app.jar
