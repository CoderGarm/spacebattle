#!/bin/bash

mysql -ukarsten < dropAndCreateSBDB.sql; mysql -ukarsten sbdb < createSBDB.sql
