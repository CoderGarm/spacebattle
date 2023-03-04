

alter table propulsion add column technologyType varchar(255) not null after hullType;

update propulsion set technologyType = 'MILITARY' where hullType != 'FR';
update propulsion set technologyType = 'CIVIL' where hullType = 'FR';

insert into dbPatch values (null, now(), 'impact ship mass to drive power', '0.0.14-1');
