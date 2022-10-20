#!/bin/bash

versionFile="version.txt"
if [ ! -f "$versionFile" ]; then
  echo "no version file specified"
  exit 1
fi

version="$(cat $versionFile)"
echo "deploying version $version"

./stopSB.sh
echo "Application stopped"

# use this to start the spring boot container and pass the parameters
java \
-Xms12G -Xmx16G \
-XX:ActiveProcessorCount=8 \
-Dlogging.pattern.console= \
-Dspring.datasource.hikari.max-lifetime=580000 \
-Dserver.port=8081 \
-Dspring.datasource.username=sbdbuser \
-Dspring.datasource.password=JYUiAsUkRQ9xQCeM \
+-Dlogging.battle-log.write=false \
+-Dlogging.rest.calls=false \
-Dlogging.file.name=/home/karsten/spacebattle/log/spacebattle.log \
-jar spacebattle-"$version".jar & echo $! > ./pid.file &

echo "Application started"
