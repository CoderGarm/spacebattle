# noinspection SqlWithoutWhereForFile

update warshipCapabilities set moduleType = 'SIDEWALL' where moduleType = 'SHIELD';
update warshipCapabilitiesSnapshot set moduleType = 'SIDEWALL' where moduleType = 'SHIELD';
update hull set hullType = 'CA' where hullType = 'CC';

alter table ammunitionModule add column hullType varchar(255) not null after techLevel;
alter table armor add column  hullType varchar(255) not null after techLevel;
alter table electronicWarfare add column  hullType varchar(255) not null after techLevel;
alter table launcher add column  hullType varchar(255) not null after techLevel;
alter table missile add column hullType varchar(255) not null after elokaResistance;
alter table missileMotor add column hullType varchar(255) not null after endurance;
alter table passiveModule add column  hullType varchar(255) not null after techLevel;
alter table propulsion add column  hullType varchar(255) not null after techLevel;
alter table sidewall add column  hullType varchar(255) not null after techLevel;
alter table warhead add column  hullType varchar(255) not null after damageValue;
alter table weapon add column  hullType varchar(255) not null after techLevel;

update ammunitionModule set hullType = 'CL';
update armor set hullType = 'CL';
update electronicWarfare set hullType = 'CL';
update launcher set hullType = 'CL';
update missile set hullType = 'CL';
update missileMotor set hullType = 'CL';
update passiveModule set hullType = 'CL';
update propulsion set hullType = 'CL';
update sidewall set hullType = 'CL';
update warhead set hullType = 'CL';
update weapon set hullType = 'CL';

insert into dbPatch values (null, now(), 'balance spacecraft stuff', '0.0.5-5');