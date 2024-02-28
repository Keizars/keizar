@echo off

set SERVER_NAME=keizar-server
set PORT=4392

call gradlew.bat installDist
docker build -t %SERVER_NAME% .
docker run -p %PORT%:%PORT% %SERVER_NAME%
