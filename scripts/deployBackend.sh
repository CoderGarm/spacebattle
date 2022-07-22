#!/bin/bash

# run it at medusa
echo '1. archive old deployment'
echo '2. deploy new stuff'

# archive
date=$(date +%F)
time=$(date +%T)
mkdir -p /home/karsten/archive/"$date"/backend/
cp /home/karsten/spacebattle/spacebattle-0.0.1-SNAPSHOT.jar /home/karsten/archive/"$date"/backend/"$time"_spacebattle-0.0.1-SNAPSHOT.jar
echo 'backend archived in ' + /home/karsten/archive/"$date"/backend/"$time"_spacebattle-0.0.1-SNAPSHOT.jar.tar.gz

# deployment
echo "start deployment"
cd /home/karsten/spacebattle/ || exit
echo "stop server"
./stopSB.sh
cp /home/karsten/uploadTarget/spacebattle-0.0.1-SNAPSHOT.jar /home/karsten/spacebattle/;
echo "start server"
./startSB.sh
echo 'deployment finished'
tail -f /home/karsten/spacebattle.log/spacebattle.log

# old style
#echo "copy deployment";
#cp uploadTarget/spacebattle-0.0.1-SNAPSHOT.jar .;
#echo "stop server";
# ./stopSB.sh;
#echo "start server";
#./startSB.sh
