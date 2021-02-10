#!/bin/bash

wget 'http://localhost:8080/v2/api-docs'; json api-docs > data/swagger.json; rm api-docs
