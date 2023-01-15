
alter table fleet drop column needsRepair;

insert into dbPatch values (null, now(), 'drop needs repair flag from fleet', '0.0.9-1');