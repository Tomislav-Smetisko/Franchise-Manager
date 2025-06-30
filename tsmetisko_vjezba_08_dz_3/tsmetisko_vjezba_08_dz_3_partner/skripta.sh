#!/bin/bash

# Zaustavi i ukloni stari kontejner ako postoji
docker stop partner_tsmetisko 2>/dev/null
docker rm partner_tsmetisko 2>/dev/null

# Build Java aplikacije s Mavenom
mvn clean package

# Izgradi novi Docker image
docker build -t partner_tsmetisko -f Dockerfile .

# Pokreni novi kontejner
docker run -it -d --network=mreza_tsmetisko --ip 20.24.5.3 \
 --name=partner_tsmetisko --hostname=partner_tsmetisko \
 --mount source=svezak_tsmetisko,target=/usr/app/podaci \
 partner_tsmetisko:latest

 docker logs -f partner_tsmetisko