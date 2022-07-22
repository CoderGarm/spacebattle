#!/bin/bash

# use this to stop the spring boot container

kill $(cat ./pid.file)
rm -f ./pid.file
