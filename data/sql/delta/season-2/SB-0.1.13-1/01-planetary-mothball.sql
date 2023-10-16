alter table warShip add column idMothball integer after idMission;

alter table warShip
   add constraint FKgxsukhuxoyaglnxcaawuyuh30
   foreign key (idMothball)
   references planet (idPlanet);

# noinspection SqlWithoutWhere
UPDATE warShip w set
    idMothball = (select p.idPlanet from planet p where p.isMain AND p.idOwner = (select s.idOwner from shipClass s WHERE s.idShipClass = w.idShipClass))
    WHERE w.isDeleted = false and w.idMission is null and w.idFleet is null;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'planetary mothball', '0.1.13-1');
