FROM adoptopenjdk/openjdk11:latest
RUN mkdir /opt/app
COPY build/libs/gs-multi-module-application-0.0.1-SNAPSHOT.jar /opt/app
CMD ["java", "-jar", "/opt/app/gs-multi-module-application-0.0.1-SNAPSHOT.jar"]