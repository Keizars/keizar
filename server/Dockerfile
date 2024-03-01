FROM openjdk:11-jre-slim

ENV TZ=Europe/London
ENV TESTING=false
# ENV TOKEN_SECRET=""
# ENV MONGODB_CONNECTION_STRING=""
ENV AWS_REGION="eu-west-1"
ENV PORT=4392

COPY ./build/install/server /keizar-server

EXPOSE $PORT

ENTRYPOINT ["/bin/bash", "/keizar-server/bin/server"]
