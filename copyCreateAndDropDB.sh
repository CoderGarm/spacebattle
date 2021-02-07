#!/bin/bash

cp /tmp/*.sql data/sql/
cat data/sql/dropSBDB.sql > data/sql/recreateSBDB.sql
cat data/sql/createSBDB.sql >> data/sql/recreateSBDB.sql
echo "done"
