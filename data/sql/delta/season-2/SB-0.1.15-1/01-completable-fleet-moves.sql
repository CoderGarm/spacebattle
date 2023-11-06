alter table move rename column moveDoneAtZero to ticksLeft;

alter table move add column isDeleted boolean not null default false after idMove;
alter table move add column idTickCompleted integer after originalDuration;
alter table move add column idFleetSnapshot integer after idFleet;

alter table move
   add constraint FKhonj0ybwu1naypgp7b5d0cwip
   foreign key (idTickCompleted)
   references tick (idTick);

alter table move
   add constraint FKuoi0o5abd3i359pmc4idclrx
   foreign key (idFleetSnapshot)
   references fleetSnapshot (idFleetSnapshot);

ALTER TABLE fleetSnapshot MODIFY COLUMN idBattleReport int;
ALTER TABLE move MODIFY COLUMN idFleet int;

alter table battleReport add column idPlanet integer after idTick;
alter table fleet add column idPlanetLocation integer after idMove;
alter table move add column idPlanetDestination integer after idTickCompleted;
alter table move add column idPlanetOrigin integer after idFleetSnapshot;

alter table battleReport
   add constraint FKry6bc39fdk37dvtpfwtljucef
   foreign key (idPlanet)
   references planet (idPlanet);

alter table fleet
   add constraint FKjn1r1mte3awql1sp7a2sehrsv
   foreign key (idPlanetLocation)
   references planet (idPlanet);

alter table move
   add constraint FKpqyl1dnhe1gc67jbcsa6i10s9
   foreign key (idPlanetDestination)
   references planet (idPlanet);

alter table move
   add constraint FK1us3my5u8r5mv1jupu0xw33fp
   foreign key (idPlanetOrigin)
   references planet (idPlanet);

update fleet f set idPlanetLocation = (SELECT p.idPlanet FROM planet p WHERE p.idStarSystem = f.idStarSystemLocation AND p.xCoordinate = f.xCoordinateLocation  AND p.yCoordinate = f.yCoordinateLocation)
    where f.xCoordinateLocation is not null;

update move m set idPlanetOrigin = (SELECT p.idPlanet FROM planet p WHERE p.idStarSystem = m.idStarSystemOrigin AND p.xCoordinate = m.xCoordinateOrigin  AND p.yCoordinate = m.yCoordinateOrigin)
    where m.xCoordinateOrigin is not null;

update move m set idPlanetDestination = (SELECT p.idPlanet FROM planet p WHERE p.idStarSystem = m.idStarSystemDestination AND p.xCoordinate = m.xCoordinateDestination  AND p.yCoordinate = m.yCoordinateDestination)
    where m.xCoordinateDestination is not null;

update move m set m.xCoordinateOrigin = null, m.yCoordinateOrigin = null where m.idPlanetOrigin is not null;
update move m set m.xCoordinateDestination = null, m.yCoordinateDestination = null where m.idPlanetDestination is not null;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'completable fleet move', '0.1.15-1');
