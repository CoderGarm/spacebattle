
/*alter table movementAction drop column xCoordinate; fixme rollback */
/*alter table movementAction drop column yCoordinate; fixme rollback */
alter table movementAction drop column xCoordDestination;
alter table movementAction drop column yCoordDestination;
alter table movementAction drop column xCoordInterimDestination;
alter table movementAction drop column yCoordInterimDestination;

 alter table movementAction add column lengthOnTrack varchar(255) not null after combatRound;

 # noinspection SqlWithoutWhere
 update movementAction set lengthOnTrack = '0 M';

    create table maneuver (
       idManeuver integer not null auto_increment,
        combatPhase varchar(255) not null,
        combatRound integer not null,
        designatedEnd integer not null,
        end integer not null,
        name varchar(255) not null,
        idActor integer not null,
        idTarget integer not null,
        primary key (idManeuver)
    ) engine=InnoDB;

    create table maneuverElement (
       idManeuverElement integer not null auto_increment,
        xCoordinateCP1 varchar(255),
        yCoordinateCP1 varchar(255),
        xCoordinateCP2 varchar(255),
        yCoordinateCP2 varchar(255),
        xCoordinateP1 varchar(255),
        yCoordinateP1 varchar(255),
        xCoordinateP2 varchar(255),
        yCoordinateP2 varchar(255),
        partOfManeuver integer not null,
        sequenceNo integer not null,
        idManeuver integer not null,
        primary key (idManeuverElement)
    ) engine=InnoDB;

 alter table movementAction add column idManeuver integer not null after idActor;

 insert into maneuver (combatPhase, combatRound, designatedEnd, end, name, idActor, idTarget) values ('MOVEMENT_PHASE', 1, 1, 30, 'INSERT MANEUVER', 1, 1);

 # noinspection SqlWithoutWhere
 update movementAction set idManeuver = 1;

    alter table maneuver
       add constraint FKcvx1o183kutmyqa1jj40bij56
       foreign key (idActor)
       references fleet (idFleet);

    alter table maneuver
       add constraint FKb3vcn9udn13s92hu6spvmbs9y
       foreign key (idTarget)
       references fleet (idFleet);

    alter table maneuverElement
       add constraint FK5rat96hasfnv0pnxdyti3nsrv
       foreign key (idManeuver)
       references maneuver (idManeuver);

    alter table movementAction
       add constraint FK88r923vtbqffqy7u3n62bw5d7
       foreign key (idManeuver)
       references maneuver (idManeuver);

INSERT INTO dbPatch VALUES (NULL, NOW(), 'switch to bezier course', '0.1.18-2');
