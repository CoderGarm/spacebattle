#!/bin/bash

mysql -ukarsten < ~/idea-workspace/spacebattle/data/sql/dropAndCreateSBDB.sql
mysql -ukarsten sbdb < medusa_sbdb_dump_just-a-dump-2023-09-09-21:27:58.sql
mysql -ukarsten sbdb < ~/idea-workspace/spacebattle/data/sql/delta/season-2/SB-0.1.10-1/01*.sql
mysql -ukarsten sbdb < localhost-passwords.sql
