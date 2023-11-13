
create table transportJob (
   idTransportJob integer not null auto_increment,
    isDeleted boolean not null default false,
    ticksLeft decimal(19, 0) not null,
    idTickCompleted integer,
    idDestination integer not null,
    idOrigin integer not null,
    idOwner integer not null,
    primary key (idTransportJob)
) engine=InnoDB;

alter table transportJob
   add constraint FK3p8d8qmsdvengg6kmvnesqipi
   foreign key (idTickCompleted)
   references tick (idTick);

alter table transportJob
   add constraint FKq08wsniijayb6pwmfolhdphsl
   foreign key (idDestination)
   references planet (idPlanet);

alter table transportJob
   add constraint FKc0d66ie3d1ungo75ft5ntsjj
   foreign key (idOrigin) 
   references planet (idPlanet);

alter table transportJob
   add constraint FK1t3cfowqftboj0yqntyki29fv
   foreign key (idOwner)
   references user (idUser);

alter table warShip add column idTransportJob integer after idMothball;


alter table warShip
   add constraint FKeyf0wphylpn2gwbgwtvvspw4h
   foreign key (idTransportJob)
   references transportJob (idTransportJob);

create table transferredShips (
   idTransportJob integer not null,
    idWarship integer not null,
    primary key (idTransportJob, idWarship)
) engine=InnoDB;

alter table transferredShips
   add constraint FKdi04kj2s8phv98b06t97b0g1b
   foreign key (idWarship)
   references warShip (idWarShip);

alter table transferredShips
   add constraint FK8nikh4493iao57u0gjtht1bkn
   foreign key (idTransportJob)
   references transportJob (idTransportJob);

INSERT INTO dbPatch VALUES (NULL, NOW(), 'transport job', '0.1.15-4');
