#!/bin/bash

mysql -ukarsten < ~/idea-workspace/spacebattle/data/sql/dropAndCreateSBDB.sql; mysql -ukarsten sbdb < medusa_sbdb_dump_after-0.0.13-2023-03-04-14:25:50.sql;
mysql -ukarsten sbdb < localhost-passwords.sql
