alter table planet drop foreign key FKgayj6n1e1tkll78se8wj08yr9;
alter table planet drop column idResourceDemand;

alter table planet drop foreign key FKchm1nm87cpqlwgayp6vhl8vux;
alter table planet drop column idResourceUtilization;

update translation set translation = 'The shipyard constructs off-planet components, ships and space stations.' where idTranslation = 83;
update translation set translation = 'Die Schiffswerft konstruiert außerplanetare Bauteile, Schiffe und Raumstationen.' where idTranslation = 87;
update translation set translation = 'Schiffswerft' where idTranslation = 88;

insert into dbPatch values (null, now(), 'drop persisted demand', '0.1.8-1');
