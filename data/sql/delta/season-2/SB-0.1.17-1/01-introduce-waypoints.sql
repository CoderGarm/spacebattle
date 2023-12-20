
update starSystem set name = 'Ueshiba' where idStarSystem = 11;
update starSystem set name = 'Homam' where idStarSystem = 132;
update starSystem set name = 'Fedora' where idStarSystem = 77;
update starSystem set name = 'Arch' where idStarSystem = 78;


update starSystem set name = "Hannah's Star" where name = 'Hannah’s Star';
update starSystem set name = "Sharon's Star" where name = 'Sharon’s Star';
update starSystem set name = "Trevor's Star" where name = 'Trevor’s Star';
update starSystem set name = "Typer's Star" where name = 'Tyler’s Star';

create table flightPlan (
   idFlightPlan integer not null auto_increment,
    xCoordinate varchar(255),
    yCoordinate varchar(255),
    timeAfterStart decimal(3, 0),
    idPlanet integer,
    idStarSystem integer,
    idMove integer,
    primary key (idFlightPlan)
) engine=InnoDB;

alter table move add column idTickStarted integer not null after idUser;
# noinspection SqlWithoutWhere
update move set idTickStarted = (select max(idTick) - (originalDuration - move.ticksLeft) from tick);


    create table waypoints (
       idStarSystem integer not null,
        idMove integer not null
    ) engine=InnoDB;

    alter table flightPlan
       add constraint FK5r9d4uu2n4b4twymkxbmg7x7b
       foreign key (idPlanet)
       references planet (idPlanet);

    alter table flightPlan
       add constraint FKf4ew7t3sk8e7uid5vq61rm0qd
       foreign key (idStarSystem)
       references starSystem (idStarSystem);

    alter table flightPlan
       add constraint FK8pnr4eib22sc2tyr556x0750w
       foreign key (idMove)
       references move (idMove);

    alter table move
       add constraint FK6f36ja23cxfke6ft2pu3j23fg
       foreign key (idTickStarted)
       references tick (idTick);


    alter table waypoints
       add constraint FK6qisbho8xyd2p6jxmmdeh9es
       foreign key (idStarSystem)
       references starSystem (idStarSystem);

    alter table waypoints
       add constraint FKclsqcqyw4yyytl2n84q0qk7pb
       foreign key (idMove)
       references move (idMove);


INSERT INTO dbPatch VALUES (NULL, NOW(), 'introduce waypoints', '0.1.17-1');
