FROM adoptopenjdk/openjdk13:jre-13.0.2_8-alpine
LABEL maintainer = sethuram
WORKDIR /usr/src/core
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8084
ENV LOCK_SERVER_HOSTNAME=localhost LOCK_SERVER_PORT=8083 ACCESS_SERVER_HOSTNAME=localhost ACCESS_SERVER_PORT=8085
ENTRYPOINT ["java","-jar","app.jar"]