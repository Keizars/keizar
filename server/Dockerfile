FROM openjdk:11-jre-slim

ENV TZ=Europe/London
ENV AWS_ACCESS_KEY_ID="AKIA47CRVVP2P3RK4QB6",
ENV AWS_SECRET_ACCESS_KEY="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",
ENV AWS_REGION="eu-west-1"
ENV PORT=4392

COPY ./build/install/server /keizar-server

EXPOSE $PORT

ENTRYPOINT ["/keizar-server/bin/server"]
