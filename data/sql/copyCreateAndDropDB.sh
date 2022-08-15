#!/bin/bash

cp /tmp/*.sql .
cat dropSBDB.sql > recreateSBDB.sql
cat createSBDB.sql >> recreateSBDB.sql

echo "use sbdbTest;" > createTestDb.sql
cat createSBDB.sql >> createTestDb.sql
echo "done"
