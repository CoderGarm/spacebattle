create table knownStarSystem (
   idOwner integer not null,
    idStarSystem integer not null,
    primary key (idOwner, idStarSystem)
) engine=InnoDB;

alter table knownStarSystem
   add constraint FK7f2b4l87ckuibg79ydiiboscm
   foreign key (idStarSystem)
   references starSystem (idStarSystem);

alter table knownStarSystem
   add constraint FK40pb0xpx1ygtqachdbn2hn6hi
   foreign key (idOwner)
   references user (idUser);

alter table planet add column colonizedAt datetime(6) after idOwner;
update planet set colonizedAt = now() where idOwner is not null;

create table colonization (
   idColonization integer not null auto_increment,
    doneAtZero integer not null,
    idPlanet integer not null,
    idUser integer not null,
    primary key (idColonization),
    check (idPlanet is not null AND idUser is not null)
) engine=InnoDB;

alter table colonization
   add constraint FKr6k79x7m4igtmpu720nfxk2mw
   foreign key (idPlanet)
   references planet (idPlanet);

alter table colonization
   add constraint FKrfuwalj6y19xvtebuy1q05pbt
   foreign key (idUser)
   references user (idUser);