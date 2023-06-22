FROM adoptopenjdk/openjdk11:jre-11.0.9_11.1-alpine
VOLUME /tmp
ADD ./d-hub-ingest-module-0.0.1-SNAPSHOT.jar /app.jar
ADD ./application-rel.yml /application-rel.yml
ADD ./logback-spring-rel.xml /logback-spring.xml
ADD ./keystore.jks /keystore.jks
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=rel"]
