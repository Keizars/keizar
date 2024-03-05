FROM openjdk:11-jre-slim

ENV TZ=Europe/London

COPY ./build/install/server /keizar-server

EXPOSE 4392

ENTRYPOINT ["/bin/bash", "/keizar-server/bin/server"]
