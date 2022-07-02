#!/bin/bash

cp /tmp/*.sql .
cat dropSBDB.sql > recreateSBDB.sql
cat createSBDB.sql >> recreateSBDB.sql
echo "done"
