alter table colonization add column isPlanned bit not null default false after doneAtZero;

insert into dbPatch values (null, now(), 'planned colonization', '0.1.5-1');
