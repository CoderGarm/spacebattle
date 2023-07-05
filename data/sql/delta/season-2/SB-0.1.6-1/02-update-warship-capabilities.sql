
update warshipCapabilities set value = 10 where value = 0;

insert into dbPatch values (null, now(), 'update warship caps', '0.1.6-2');
