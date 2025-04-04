#!/bin/bash

# Erstelle einen Build-Container
echo "Baue das Plugin mit Docker..."
docker build -t teamspeak-integration-build -f Dockerfile.build .

# Erstelle einen temporären Container und extrahiere die JAR-Datei
echo "Extrahiere die JAR-Datei..."
CONTAINER_ID=$(docker create teamspeak-integration-build)
docker cp $CONTAINER_ID:/output/teamspeak-integration.jar ./teamspeak-integration.jar
docker rm $CONTAINER_ID

echo "Build abgeschlossen! Die JAR-Datei wurde als 'teamspeak-integration.jar' im aktuellen Verzeichnis gespeichert." 
