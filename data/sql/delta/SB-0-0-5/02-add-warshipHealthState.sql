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

create table warshipHealthState (
   idWarshipHealthState integer not null auto_increment,
    armorState integer not null,
    elokaState integer not null,
    hullState integer not null,
    propulsionState integer not null,
    sidewallState integer not null,
    idWarship integer,
    primary key (idWarshipHealthState)
) engine=InnoDB;

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

insert into dbPatch values (null, now(), 'add warship health state', '0.0.5-2');