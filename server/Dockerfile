FROM openjdk:11-jre-slim

ENV TZ=Europe/London
ENV TESTING=true
ENV MONGODB_CONNECTION_STRING=""
ENV AWS_REGION="eu-west-1"
ENV PORT=4392

COPY ./build/install/server /keizar-server

EXPOSE $PORT

ENTRYPOINT ["/keizar-server/bin/server"]
