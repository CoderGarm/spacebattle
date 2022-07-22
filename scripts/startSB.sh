#!/bin/bash

# use this to start the spring boot container and pass the parameters

java \
-Xms12G -Xmx16G \
-XX:ActiveProcessorCount=8 \
-Dlogging.pattern.console= \
-Dspring.datasource.hikari.max-lifetime=580000 \
-Dserver.port=8081 \
-Dspring.datasource.username=sbdbuser \
-Dspring.datasource.password=JYUiAsUkRQ9xQCeM \
-Dlogging.battle-log.write=false \
-Dlogging.file.name=/home/karsten/spacebattle/log/spacebattle.log \
-jar spacebattle-0.0.1-SNAPSHOT.jar & echo $! > ./pid.file &
