create table warshipHealthState (
       idWarshipHealthState integer not null auto_increment,
        isFightingCapable bit not null default true,
        idWarship integer not null,
        primary key (idWarshipHealthState)
) engine=InnoDB;

create table warshipHealthStateSnapshot (
       idWarshipHealthStateSnapshot integer not null auto_increment,
        isFightingCapable bit not null default true,
        idBattleReport integer not null,
        idFleetSnapshot integer not null,
        idWarship integer not null,
        primary key (idWarshipHealthStateSnapshot)
    ) engine=InnoDB;

create table fleetSnapshot (
       idFleetSnapshot integer not null auto_increment,
        isDeleted bit not null default false,
        name varchar(255) not null,
        idBattleReport integer not null,
        idFleet integer not null,
        idOwner integer not null,
        primary key (idFleetSnapshot)
) engine=InnoDB;



create table activeFittings (
   idWarshipHealthState integer not null,
    amount integer not null,
    idLauncher integer,
    idWeapon integer,
    weaponAlignment varchar(255)
) engine=InnoDB;

create table remainingShots (
   idWarshipHealthState integer not null,
    amount decimal(19, 0) not null,
    idMissile integer not null,
    primary key (idWarshipHealthState, idMissile)
) engine=InnoDB;

create table warshipCapabilities (
   idWarshipHealthState integer not null,
    moduleType varchar(255),
    value decimal(19, 0)
) engine=InnoDB;


create table remainingShotsSnapshot (
       idWarshipHealthStateSnapshot integer not null,
        amount decimal(19, 0) not null,
        idMissile integer not null,
        primary key (idWarshipHealthStateSnapshot, idMissile)
) engine=InnoDB;

create table warshipCapabilitiesSnapshot (
       idWarshipHealthStateSnapshot integer not null,
        moduleType varchar(255),
        value decimal(19, 0)
) engine=InnoDB;

create table activeFittingsSnapshot (
       idWarshipHealthStateSnapshot integer not null,
        amount integer not null,
        idLauncher integer,
        idWeapon integer,
        weaponAlignment varchar(255)
) engine=InnoDB;

alter table participatingFleets add column idFleetSnapshot integer not null after idBattleReport;

alter table lossesByHit add column idWarship integer not null after idFleet;

update lossesByHit set idWarship = (select idWarShip from warShip where warShip.idFleet = lossesByHit.idFleet and warShip.name = lossesByHit.warShipName);




DELIMITER |
CREATE PROCEDURE createNewBattleReports()
BEGIN
    DECLARE amount_battleReports INT DEFAULT 0;
    DECLARE iterator_battleReport INT DEFAULT 0;
    DECLARE amount_fleets INT DEFAULT 0;
    DECLARE iterator_fleets INT DEFAULT 0;
    DECLARE amount_ships INT DEFAULT 0;
    DECLARE iterator_ship INT DEFAULT 0;
    SELECT COUNT(*) FROM battleReport INTO amount_battleReports;
    SET iterator_battleReport = 0;
    WHILE iterator_battleReport < amount_battleReports
        DO
            select b.idBattleReport from battleReport b order by b.idBattleReport offset iterator_battleReport rows fetch next 1 row only into @idBattleReport;

            SELECT COUNT(*) FROM participatingFleets pf where pf.idBattleReport = @idBattleReport into amount_fleets;
            SET iterator_fleets = 0;
                WHILE iterator_fleets < amount_fleets
                    DO
                        select pf.idFleet, f.name, f.idOwner from participatingFleets pf
                            left join fleet f on (f.idFleet = pf.idFleet)
                            where pf.idBattleReport = @idBattleReport offset iterator_fleets rows fetch next 1 row only into @idFleet, @fleetName, @idOwner;

                        insert into fleetSnapshot values (null, false, @fleetName, @idBattleReport, @idFleet, @idOwner);

                        select LAST_INSERT_ID() into @idFleetSnapshot;
                        update participatingFleets set idFleetSnapshot = @idFleetSnapshot where idFleet = @idFleet;

                        SELECT COUNT(*) FROM warShip ws where ws.idFleet = @idFleet into amount_ships;
                        SET iterator_ship = 0;
                        WHILE iterator_ship < amount_ships
                            DO
                                SELECT ws.idWarShip FROM warShip ws where ws.idFleet = @idFleet offset iterator_ship rows fetch next 1 row only into @idWarship;

                                insert into warshipHealthStateSnapshot values (null, true, @idBattleReport, @idFleetSnapshot, @idWarship);
                            SET iterator_ship = iterator_ship + 1;
                        END WHILE;
                    SET iterator_fleets = iterator_fleets + 1;
                END WHILE;
            SET iterator_battleReport = iterator_battleReport + 1;
        END WHILE;
End |
DELIMITER ;

call createNewBattleReports();
drop procedure createNewBattleReports;

alter table fleetSnapshot
   add constraint FK929r4p7vk0f3k3s4ocbt7h50e
   foreign key (idBattleReport)
   references battleReport (idBattleReport);

alter table fleetSnapshot
   add constraint FK7m974jpjlnp7r615irb6nppcj
   foreign key (idFleet)
   references fleet (idFleet);

alter table fleetSnapshot
   add constraint FKhr16a2b5d1q9yjjnc43holh2p
   foreign key (idOwner)
   references user (idUser);

alter table warshipCapabilities
   add constraint FKcx1bs2mh0pk76hg4yvq57vy71
   foreign key (idWarshipHealthState)
   references warshipHealthState (idWarshipHealthState);

alter table activeFittings
   add constraint FK8g2ipqwq1getx5n0fmma0unx9
   foreign key (idLauncher)
   references launcher (idLauncher);

alter table activeFittings
   add constraint FK7ek5ao1cpqrnw6fjlkpsl95e5
   foreign key (idWeapon)
   references weapon (idWeapon);

alter table activeFittings
   add constraint FKp98j3powqokonka7fwhqdlw0a
   foreign key (idWarshipHealthState)
   references warshipHealthState (idWarshipHealthState);

alter table remainingShots
   add constraint FKsucjuvh9f5mahyes6ikkbq8rr
   foreign key (idMissile)
   references missile (idMissile);

alter table remainingShots
   add constraint FK42u7h8yp1byrq1mqlgwcd2nph
   foreign key (idWarshipHealthState)
   references warshipHealthState (idWarshipHealthState);

alter table warshipHealthState
   add constraint FK8n2fodpdy927lvcfqgsh1ejc8
   foreign key (idWarship)
   references warShip (idWarShip);

alter table activeFittingsSnapshot
   add constraint FKlw1s63u5x690vuag5ym70upqw
   foreign key (idLauncher)
   references launcher (idLauncher);

alter table activeFittingsSnapshot
   add constraint FK8ug5h1r2xixwuogyl5n6y2jy2
   foreign key (idWeapon)
   references weapon (idWeapon);

alter table activeFittingsSnapshot
   add constraint FKm7x7patbu0qiko7r2v7hdnoi
   foreign key (idWarshipHealthStateSnapshot)
   references warshipHealthStateSnapshot (idWarshipHealthStateSnapshot);

alter table remainingShotsSnapshot
   add constraint FKcfct28ygeri834a9akuiafjqg
   foreign key (idMissile)
   references missile (idMissile);

alter table remainingShotsSnapshot
   add constraint FKowghx8dytftgsrejrwv13qlbf
   foreign key (idWarshipHealthStateSnapshot)
   references warshipHealthStateSnapshot (idWarshipHealthStateSnapshot);

alter table warshipCapabilitiesSnapshot
   add constraint FKeoahcn00mc9xyot7w9bqpcsof
   foreign key (idWarshipHealthStateSnapshot)
   references warshipHealthStateSnapshot (idWarshipHealthStateSnapshot);

alter table warshipHealthStateSnapshot
   add constraint FKkohi791t1w85m3utjw493xcbe
   foreign key (idBattleReport)
   references battleReport (idBattleReport);

alter table warshipHealthStateSnapshot
   add constraint FKcjim226ew093h6wpualjjrodk
   foreign key (idFleetSnapshot)
   references fleetSnapshot (idFleetSnapshot);

alter table warshipHealthStateSnapshot
   add constraint FKboga6909c5cwey1d6ung5i8we
   foreign key (idWarship)
   references warShip (idWarShip);

alter table participatingFleets drop foreign key if exists FKayayypcvevpaihludw9p2jcdh;

alter table participatingFleets
   add constraint FKptc0phylec3318d12arsxd0j
   foreign key (idFleetSnapshot)
   references fleetSnapshot (idFleetSnapshot);

alter table participatingFleets drop primary key, add primary key(idBattleReport, idFleetSnapshot);
alter table participatingFleets drop column idFleet;

insert into dbPatch values (null, now(), 'add warship health state', '0.0.5-2');
