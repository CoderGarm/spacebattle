alter table job rename column ticksLeft to pointsLeft;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'exchange ticks left to points', '0.1.14-4');
