#!/bin/bash

timestamp=$(date +%s)
diff createSBDB.sql /tmp/createSBDB.sql > delta/"$timestamp"_create.sql
diff dropSBDB.sql /tmp/dropSBDB.sql > delta/"$timestamp"_drop.sql
echo "done"
echo ''
echo ''
echo 'please adjust the diff files manually'
