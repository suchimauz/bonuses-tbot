FROM openjdk:11-jre-slim-buster
EXPOSE 8080

ADD target/uberjar/tbot-0.1.0-SNAPSHOT-standalone.jar /app.jar
ADD resources /resources

CMD java -jar /app.jar
