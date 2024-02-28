FROM openjdk:11-jre-slim

ENV TZ=Europe/London
# ENV AWS_ACCESS_KEY_ID="",
# ENV AWS_SECRET_ACCESS_KEY="",
ENV AWS_REGION="eu-west-1"
ENV PORT=4392

COPY ./build/install/server /keizar-server

EXPOSE $PORT

ENTRYPOINT ["/bin/bash", "/keizar-server/bin/server"]
