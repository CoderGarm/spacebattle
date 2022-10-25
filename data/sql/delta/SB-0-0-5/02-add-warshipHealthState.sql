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

create table warshipHealthState (
   idWarshipHealthState integer not null auto_increment,
    isFightingCapable bit not null default true,
    idWarship integer not null,
    primary key (idWarshipHealthState)
) engine=InnoDB;

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

alter table lossesByHit add column idWarship integer not null after idFleet;

update lossesByHit set idWarship = (select idWarShip from sbdb.warShip where warShip.idFleet = lossesByHit.idFleet and warShip.name = lossesByHit.warShipName);

insert into dbPatch values (null, now(), 'add warship health state', '0.0.5-2');