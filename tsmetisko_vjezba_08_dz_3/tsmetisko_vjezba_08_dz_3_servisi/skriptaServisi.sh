#!/bin/bash

# Zaustavi i ukloni stari kontejner ako postoji
docker stop servis_tsmetisko 2>/dev/null
docker rm servis_tsmetisko 2>/dev/null

# Build Java aplikacije s Mavenom
mvn clean package

# Izgradi novi Docker image
docker build -t servis_tsmetisko -f Dockerfile .

# Pokreni novi kontejner
docker run -it -d \
--network=mreza_tsmetisko \
--ip 20.24.5.20 \
--name=servis_tsmetisko \
--hostname=servis_tsmetisko \
servis_tsmetisko:latest

docker logs -f servis_tsmetisko
