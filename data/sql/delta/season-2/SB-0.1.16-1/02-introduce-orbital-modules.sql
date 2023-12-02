
create table orbitalModule (
   idOrbitalModule integer not null auto_increment,
    techLevel varchar(255) not null,
    baseValue integer not null,
    effect varchar(255) not null,
    unlockedThroughLevel integer not null,
    idTranslatableDescription integer not null,
    idTranslatableName integer not null,
    idCosts integer not null,
    idResearch integer not null,
    primary key (idOrbitalModule)
) engine=InnoDB;

create table orbitalModuleJobElements (
   idJob integer not null,
    amount integer not null,
    idOrbitalModule integer
) engine=InnoDB;

alter table orbitalModule
   add constraint FK96s9g1xhrqa6mb0vdfptri16j
   foreign key (idTranslatableDescription)
   references translatable (idTranslatable);

alter table orbitalModule
   add constraint FKjnv18sflhrm4m6dwn86ewb80
   foreign key (idTranslatableName)
   references translatable (idTranslatable);

alter table orbitalModule
   add constraint FKjkhnt794699qh7ruud29r2tt
   foreign key (idCosts)
   references resourceDeposit (idResourceDeposit);

alter table orbitalModule
   add constraint FK2nshm5a979bu2dnqk8wswlqcm
   foreign key (idResearch)
   references research (idResearch);

alter table orbitalModuleJobElements
   add constraint FKhr85m1salx7enm27qj75gwg63
   foreign key (idOrbitalModule)
   references orbitalModule (idOrbitalModule);

alter table orbitalModuleJobElements
   add constraint FKagpanf4seayijqw1ywrfs5ds4
   foreign key (idJob)
   references job (idJob);


create table orbitalStructure (
   idOrbitalStructure integer not null auto_increment,
    isDeleted boolean not null default false,
    isOperational boolean not null default false,
    amount integer not null,
    xCoordinate varchar(255),
    yCoordinate varchar(255),
    idOrbitalModule integer not null,
    idPlanet integer,
    idStarSystem integer,
    idOwner integer not null,
    primary key (idOrbitalStructure)
) engine=InnoDB;

alter table orbitalStructure
   add constraint FKhd287pobvlknx1eo1b9rrix9x
   foreign key (idOrbitalModule)
   references orbitalModule (idOrbitalModule);

alter table orbitalStructure
   add constraint FK4kmotox08ph1avbt2f98l4x5
   foreign key (idPlanet)
   references planet (idPlanet);

alter table orbitalStructure
   add constraint FKnia95vlkjkyjdrqw3qwi1dt4e
   foreign key (idStarSystem)
   references starSystem (idStarSystem);

alter table orbitalStructure
   add constraint FK89io2i6h4mwlhimons9paalqh
   foreign key (idOwner)
   references user (idUser);

alter table job drop constraint job_CHECK;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'introduce orbital modules', '0.1.16-2');
