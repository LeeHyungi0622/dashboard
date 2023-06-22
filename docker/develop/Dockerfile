FROM adoptopenjdk/openjdk11:jre-11.0.9_11.1-alpine
VOLUME /tmp
ADD ./jars/d-hub-ingest-module-0.0.1-SNAPSHOT.jar /app.jar
ADD ./jars/application-dev.yml /application-dev.yml
ADD ./jars/logback-spring-dev.xml /logback-spring.xml
ADD keystore.jks /keystore.jks
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=dev"]
