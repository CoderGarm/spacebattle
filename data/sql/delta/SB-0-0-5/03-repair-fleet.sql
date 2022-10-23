
alter table fleet add column needsRepair bit not null default false after name;

alter table job add column priority varchar(255) after jobDoneAtZero;
alter table job add column idFleet integer after idBuilding;

alter table job
       add constraint FK9cgvto0bqandfg7ly93veyvc5
       foreign key (idFleet)
       references fleet (idFleet);

insert into dbPatch values (null, now(), 'repair fleets by job', '0.0.5-3');