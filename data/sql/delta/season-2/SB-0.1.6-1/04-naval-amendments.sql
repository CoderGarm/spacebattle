
alter table warShip modify column idFleet integer;

alter table warShip add column idMission integer after idFleet;

create table mission (
   missionType varchar(31) not null,
    idMission integer not null auto_increment,
    isDeleted boolean not null default false,
    idActor integer not null,
    idTickStartedAt integer not null,
    idTickStoppedAt integer,
    idPlanet integer not null,
    primary key (idMission)
) engine=InnoDB;

alter table mission
   add constraint FKbvhlv330gufbb2p7aeeyagtu8
   foreign key (idActor)
   references user (idUser);

alter table mission
   add constraint FKav28cevdimw8uypty1f3sgu3c
   foreign key (idTickStartedAt)
   references tick (idTick);

alter table mission
   add constraint FKfjo5uacvj75iku0n27y8f781q
   foreign key (idTickStoppedAt)
   references tick (idTick);

alter table mission
   add constraint FKholrjg4864rt9j8qqs349b7ue
   foreign key (idPlanet)
   references planet (idPlanet);

alter table warShip
   add constraint FKr1fjudewjfngri3nq6axpbf5r
   foreign key (idMission)
   references mission (idMission);

insert into dbPatch values (null, now(), 'naval amendments', '0.1.6-4');
