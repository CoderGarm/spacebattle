#!/bin/bash

# run it at medusa
echo '1. archive old deployment'
echo '2. deploy new stuff'

versionFile="spacebattle/version.txt"
if [ ! -f "$versionFile" ]; then
  echo "no version file specified"
  exit 1
fi

version="$(cat $versionFile)"
echo "deploying version $version"

# archive
date=$(date +%F)
time=$(date +%T)
mkdir -p /home/karsten/archive/"$date"/backend/"$time"/
mv /home/karsten/spacebattle/spacebattle-*.jar /home/karsten/archive/"$date"/backend/"$time"/
echo 'backend archived in ' + /home/karsten/archive/"$date"/backend/"$time"/

# deployment
echo "start deployment"
cd /home/karsten/spacebattle/ || exit
echo "stop server"
./stopSB.sh
cp /home/karsten/uploadTarget/spacebattle-"$version".jar /home/karsten/spacebattle/
echo "start server"
./startSB.sh
echo 'deployment finished'
