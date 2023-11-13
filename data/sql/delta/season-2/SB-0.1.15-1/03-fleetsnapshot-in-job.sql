alter table job add column idFleetSnapshot integer after idFleet;

ALTER TABLE job DROP CONSTRAINT job_CHECK;
ALTER TABLE job ADD constraint job_CHECK check ((idBuilding IS NOT NULL AND targetLevel IS NOT NULL) OR (idResearch IS NOT NULL AND targetLevel IS NOT NULL) OR (idFleet IS NOT NULL OR idFleetSnapshot IS NOT NULL) );

alter table job
   add constraint FK7qelga4rbeqyxcbvr96lwcwh7
   foreign key (idFleetSnapshot)
   references fleetSnapshot (idFleetSnapshot);

alter table warshipHealthStateSnapshot drop constraint FKkohi791t1w85m3utjw493xcbe;
ALTER TABLE warshipHealthStateSnapshot drop COLUMN idBattleReport;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'fleet snap in job', '0.1.15-3');
