
alter table fleet add column needsRepair bit not null default false after name;

alter table job add column priority varchar(255) not null after jobDoneAtZero;
alter table job add column idFleet integer after idBuilding;

update job set priority = 'NONE' where priority is null;

alter table job add column isDeleted bit not null default false after idJob;

alter table job add column idTick integer after idFacility;

alter table job
       add constraint FK9cgvto0bqandfg7ly93veyvc5
       foreign key (idFleet)
       references fleet (idFleet);

alter table job
       add constraint FKe2jgcwt8phugfp1aj5bu76132
       foreign key (idTick)
       references tick (idTick);

insert into dbPatch values (null, now(), 'repair fleets by job', '0.0.5-3');