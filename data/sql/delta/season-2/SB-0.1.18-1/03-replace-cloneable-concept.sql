
alter table releasedVolley drop COLUMN initialDistance;
alter table shipKillerHit drop COLUMN distance;
alter table missileMovement drop COLUMN xCoordLast;
alter table missileMovement drop COLUMN yCoordLast;
alter table counterMissileHit drop COLUMN remainingMissiles;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'drop cloneable concept', '0.1.18-3');
