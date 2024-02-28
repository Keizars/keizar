#!/bin/sh

SERVER_NAME="keizar-server"
PORT=4392

./gradlew installDist
docker build . -t $SERVER_NAME
docker run -p $PORT:$PORT $SERVER_NAME