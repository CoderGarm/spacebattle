#!/bin/bash

date=$(date +%F)
time=$(date +%T)
timestamp="$date-$time"
nameComplement=''

if [ -z "$1" ]
  then
    nameComplement=$timestamp
  else
    nameComplement="$1-$timestamp"
fi

# shellcheck disable=SC2024
sudo mysqldump sbdb > "/home/karsten/dumps/medusa_sbdb_dump_$nameComplement.sql"
