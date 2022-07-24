#!/bin/bash

mvn clean install -DskipTests; scp target/spacebattle-0.0.1-SNAPSHOT.jar medusa:uploadTarget/
