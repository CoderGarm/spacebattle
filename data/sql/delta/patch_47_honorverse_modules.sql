 create table alignedFitting (
   idShipClass integer not null,
    amount integer not null,
    idWeapon integer not null,
    weaponAlignment varchar(255)
) engine=InnoDB;

create table armor (
   idArmor integer not null auto_increment,
    description varchar(255) not null,
    effectValue integer not null,
    name varchar(30) not null,
    techLevel integer not null,
    useCapacity integer not null,
    idCosts integer not null,
    idResearch integer not null,
    primary key (idArmor)
) engine=InnoDB;

create table propulsion (
   idPropulsion integer not null auto_increment,
    description varchar(255) not null,
    effectValue integer not null,
    name varchar(30) not null,
    techLevel integer not null,
    useCapacity integer not null,
    ftlCapable bit not null,
    idCosts integer not null,
    idResearch integer not null,
    primary key (idPropulsion)
) engine=InnoDB;

create table sidewall (
   idSidewall integer not null auto_increment,
    description varchar(255) not null,
    effectValue integer not null,
    name varchar(30) not null,
    techLevel integer not null,
    useCapacity integer not null,
    idCosts integer not null,
    idResearch integer not null,
    primary key (idSidewall)
) engine=InnoDB;

create table weapon (
   idWeapon integer not null auto_increment,
    description varchar(255) not null,
    effectValue integer not null,
    name varchar(30) not null,
    techLevel integer not null,
    useCapacity integer not null,
    allowedForBow integer not null,
    allowedForBroadsides integer not null,
    allowedForStern integer not null,
    damageType varchar(255) not null,
    effectiveRange integer not null,
    sideWallPenetration decimal(19, 5),
    weaponType varchar(255) not null,
    idCosts integer not null,
    idResearch integer not null,
    primary key (idWeapon)
) engine=InnoDB;

create table electronicWarfare (
   idElectronicWarfare integer not null auto_increment,
    description varchar(255) not null,
    effectValue integer not null,
    name varchar(30) not null,
    techLevel integer not null,
    useCapacity integer not null,
    idCosts integer not null,
    idResearch integer not null,
    primary key (idElectronicWarfare)
) engine=InnoDB;

alter table shipClass add column idElectronicWarfare integer after idOwner;
alter table shipClass add column idSidewall integer after idOwner;
alter table shipClass add column idArmor integer after idOwner;
alter table shipClass add column idPropulsion integer not null after idOwner;
alter table shipClass drop column raceType;
alter table user drop column raceType;

alter table hull add column constructionCapacityBroadsides integer not null after constructionCapacity;
alter table hull add column constructionCapacityStern integer not null after constructionCapacity;
alter table hull add column constructionCapacityBow integer not null after constructionCapacity;
alter table hull add column hullType varchar(255) not null  after constructionCapacity;

delete from resourceDeposit where idResourceDeposit in (select idCosts from module);

delete from moduleComposition;
delete from module;
drop table moduleComposition;
drop table module;

alter table alignedFitting
   add constraint FKt6aos80sh8332mepbkuwmo98i
   foreign key (idWeapon)
   references weapon (idWeapon);

alter table alignedFitting
   add constraint FKgdp5e1ylgswr29e2d5b7uhib
   foreign key (idShipClass)
   references shipClass (idShipClass);

alter table armor
   add constraint FK10dhr7h3pkps3d7u22q2pwpgc
   foreign key (idCosts)
   references resourceDeposit (idResourceDeposit);

alter table armor
   add constraint FKrb3h67mjdni459t4j1y8b7sw5
   foreign key (idResearch)
   references research (idResearch);

alter table electronicWarfare
   add constraint FKccj76id0r5pq3p7f4viriwdqf
   foreign key (idCosts)
   references resourceDeposit (idResourceDeposit);

alter table electronicWarfare
   add constraint FKhr2adrrpeb3vshv11ajrgnkd7
   foreign key (idResearch)
   references research (idResearch);

alter table propulsion
   add constraint FKqjsvyhjc6w21niim4aeptpm85
   foreign key (idCosts)
   references resourceDeposit (idResourceDeposit);

alter table propulsion
   add constraint FK7rr2gvpcbjjhl9tuxe6c50v5q
   foreign key (idResearch)
   references research (idResearch);

alter table sidewall
   add constraint FKlo0i3byallqh89wd535yrbs3l
   foreign key (idCosts)
   references resourceDeposit (idResourceDeposit);

alter table sidewall
   add constraint FK693a9gix6ifpkiop612tghdy0
   foreign key (idResearch)
   references research (idResearch);

alter table weapon
   add constraint FK1rsb3ampiw8yjy8ngrget6ay
   foreign key (idCosts)
   references resourceDeposit (idResourceDeposit);

alter table weapon
   add constraint FKo22n18dgjpraqosj7nkamrnvb
   foreign key (idResearch)
   references research (idResearch);

alter table shipClass
   add constraint FKouxjssb18x4jeutl5r1l0byeu
   foreign key (idArmor)
   references armor (idArmor);

alter table shipClass
   add constraint FKdd7voavc2cml9rodxm6vnlaqq
   foreign key (idPropulsion)
   references propulsion (idPropulsion);

alter table shipClass
   add constraint FKsa1b1j6ur2emh3jv7s0ft3nru
   foreign key (idSidewall)
   references sidewall (idSidewall);

alter table shipClass
       add constraint FKfbii11hday9qcjpmi2i1k2611
       foreign key (idElectronicWarfare)
       references electronicWarfare (idElectronicWarfare);
