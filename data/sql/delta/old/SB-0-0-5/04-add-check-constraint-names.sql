ALTER TABLE forum DROP CONSTRAINT CONSTRAINT_1;
ALTER TABLE forum ADD constraint forum_CHECK check (idAlliance IS NOT NULL || role IS NOT NULL);
ALTER TABLE hull DROP CONSTRAINT CONSTRAINT_1;
ALTER TABLE hull ADD constraint hull_CHECK check (overallConstructionCapacity >= constructionCapacity + constructionCapacityBow + constructionCapacityStern + constructionCapacityBroadsides);
ALTER TABLE job DROP CONSTRAINT CONSTRAINT_1;
ALTER TABLE job ADD constraint job_CHECK check ((idBuilding IS NOT NULL AND targetLevel IS NOT NULL) OR (idResearch IS NOT NULL AND targetLevel IS NOT NULL) OR (idShipClass IS NOT NULL AND amountShips IS NOT NULL) OR (idFleet IS NOT NULL) );
ALTER TABLE launcher DROP CONSTRAINT CONSTRAINT_1;
ALTER TABLE launcher ADD constraint launcher_CHECK check (weaponType = 'MISSILE' || weaponType = 'COUNTER_MISSILE');
ALTER TABLE move DROP CONSTRAINT CONSTRAINT_1;
ALTER TABLE move ADD constraint move_CHECK check (xCoordinateOrigin != xCoordinateDestination && yCoordinateOrigin != yCoordinateDestination);
ALTER TABLE weapon DROP CONSTRAINT CONSTRAINT_1;
ALTER TABLE weapon ADD constraint weapon_CHECK check (weaponType = 'BEAM' || weaponType = 'POINT_DEFENSE');

insert into dbPatch values (null, now(), 'add check constraint names', '0.0.5-4');