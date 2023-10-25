update building set increasingFactorPerLevel = 0.4 where productionTarget = 'POPULATION';

update building set baseValue = 30 where refinementSequence = 'EDUCATION_MILITARY_I';
update building set baseValue = 10 where refinementSequence = 'EDUCATION_MILITARY_II';

delete from job where idBuilding = 7;
delete from construction where idBuilding = 7;
delete from building where idBuilding = 7;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'increase pop output', '0.1.14-3');
