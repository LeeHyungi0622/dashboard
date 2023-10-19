FROM adoptopenjdk/openjdk11:jre-11.0.9_11.1-alpine
VOLUME /tmp
ARG version
ADD ./d-hub-ingest-module-${version}.jar /app.jar
ADD ./application-rel.yml /application-rel.yml
ADD ./logback-spring-rel.xml /logback-spring-rel.xml
ADD ./keystore.jks /keystore.jks
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=rel,common"]
