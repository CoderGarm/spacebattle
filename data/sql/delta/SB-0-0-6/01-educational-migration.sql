alter table construction add column operationalLevel integer not null after level;
alter table fleet add column isOperational bit not null default false after isDeleted;
alter table job add column isRepairJob bit not null default false after isDeleted;
alter table planet add column idResourceDemand integer after idOwner;
alter table planet add column idResourceUtilization integer after idResourceDeposit;


DELIMITER |
CREATE PROCEDURE insertResourceStuff()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM planet INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select p.idPlanet from planet p offset i rows fetch next 1 row only into @idPlanet;

            insert into resourceDeposit values (null, 'DEMAND');
            select LAST_INSERT_ID() into @idR;
            update planet set idResourceDemand = @idR where idPlanet = @idPlanet;

            insert into resourceDeposit values (null, 'UTILIZATION');
            select LAST_INSERT_ID() into @idR;
            update planet set idResourceUtilization = @idR where idPlanet = @idPlanet;

            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertResourceStuff();
drop procedure insertResourceStuff;

alter table warShip add column isOperational bit not null default false after isDeleted;
update warShip w set w.isDeleted = true where w.isDeleted = false and true = (select s.isDeleted from shipClass s where s.idShipClass = w.idShipClass);

alter table planet
       add constraint FKgayj6n1e1tkll78se8wj08yr9
       foreign key (idResourceDemand)
       references resourceDeposit (idResourceDeposit);

alter table planet
       add constraint FKchm1nm87cpqlwgayp6vhl8vux
       foreign key (idResourceUtilization)
       references resourceDeposit (idResourceDeposit);

update job j set j.idFleet = (select distinct idFleet from fleet f where f.idOwner = j.idOwner limit 1) where idFleet is null and idShipClass is not null;

alter table job drop constraint job_CHECK;
alter table job drop foreign key FKsevbhc9015r9wmqvojq1dbsen;

alter table job drop column idShipClass;
alter table job drop column amountShips;

ALTER TABLE job ADD constraint job_CHECK check ((idBuilding IS NOT NULL AND targetLevel IS NOT NULL) OR (idResearch IS NOT NULL AND targetLevel IS NOT NULL) OR (idFleet IS NOT NULL) );

# noinspection SqlWithoutWhere
update construction set operationalLevel = 0;

insert into dbPatch values (null, now(), 'add operationals', '0.0.6-1');