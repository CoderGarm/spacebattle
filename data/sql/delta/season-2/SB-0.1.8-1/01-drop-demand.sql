alter table planet drop foreign key FKgayj6n1e1tkll78se8wj08yr9;
alter table planet drop column idResourceDemand;

insert into dbPatch values (null, now(), 'drop persisted demand', '0.1.8-1');
