#!/bin/bash

rm -r out/artifacts/*
mvn clean install
echo "done"
echo "to see rest api just copy content from http://localhost:8080/v2/api-docs to local swagger-editor"