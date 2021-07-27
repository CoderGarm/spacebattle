# Battle for Honor

### An honor harrington browser game

Just a simple 4X game which is based on the honorverse created by David Weber.

### System distribution

This is the backend of the game.

The frontend is located in [another repository](https://github.com/CoderGarm/bfh-frontend).

### Starting the backend for localhost

Set up the database and the application:

1. Check it out, built it, install mariaDB
2. create the database 'sbdb'
3. pipe [createSBDB.sql](data/sql/createSBDB.sql) into the db
4. run the test [createInitialData()](src/test/java/de/yuga/spacebattle/backend/services/MasterOfTheUniverseServiceTest.java)
5. start the application

Set up the frontend:

1. check it out
2. install nodeJS and npm
3. run 'npm start' in base directory

Open the browser at the [start page](http://localhost:4200/#).

