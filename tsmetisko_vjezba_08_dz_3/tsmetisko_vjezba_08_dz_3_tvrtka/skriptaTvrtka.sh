#!/bin/bash

# Zaustavi i ukloni stari kontejner ako postoji
docker stop tvrtka_tsmetisko 2>/dev/null
docker rm tvrtka_tsmetisko 2>/dev/null

# Build Java aplikacije s Mavenom
mvn clean package

# Izgradi novi Docker image
docker build -t tvrtka_tsmetisko -f Dockerfile .

# Pokreni novi kontejner
docker run -it -d --network=mreza_tsmetisko --ip 20.24.5.2 \
--name=tvrtka_tsmetisko --hostname=tvrtka_tsmetisko \
--mount source=svezak_tsmetisko,target=/usr/app/podaci \
tvrtka_tsmetisko:latest

 docker logs -f tvrtka_tsmetisko