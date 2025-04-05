@echo off
echo Baue das Plugin mit Docker...
docker build -t teamspeak-integration-build -f Dockerfile.build .

echo Extrahiere die JAR-Datei...
for /f "tokens=*" %%a in ('docker create teamspeak-integration-build') do set CONTAINER_ID=%%a
docker cp %CONTAINER_ID%:/output/teamspeak-integration.jar ./minecraft-plugin/target/teamspeak-integration.jar
docker rm %CONTAINER_ID%

echo Build abgeschlossen! Die JAR-Datei wurde als 'teamspeak-integration.jar' im aktuellen Verzeichnis gespeichert. 
