FROM adoptopenjdk/openjdk11:jre-11.0.9_11.1-alpine
VOLUME /tmp
ARG version
ADD ./d-hub-ingest-module-${version}.jar /app.jar
ADD ./application-dev.yml /application-dev.yml
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=dev,common"]
