
create table allianceApplication (
   idAllianceApplication integer not null auto_increment,
    applicationState varchar(255) not null,
    idAlliance integer not null,
    idUser integer not null,
    idTickAppliedAt integer not null,
    idTickDecidedAt integer,
    primary key (idAllianceApplication)
) engine=InnoDB;

 alter table allianceApplication
   add constraint FK1awqwgyqyd6ij150b4dwhoa8t
   foreign key (idAlliance)
   references alliance (idAlliance);

alter table allianceApplication
   add constraint FKibkneamgqu1yjkxmtn09gn7ct
   foreign key (idUser)
   references user (idUser);

alter table allianceApplication
   add constraint FK90anukwo3tqyrbwum7veaglgp
   foreign key (idTickAppliedAt)
   references tick (idTick);

alter table allianceApplication
   add constraint FKn3vxrm2s3ytx9ivggr9fflnqh
   foreign key (idTickDecidedAt)
   references tick (idTick);

insert into allianceApplication (idAllianceApplication, applicationState, idAlliance, idUser, idTickAppliedAt)
    select null, 'OPEN', idAlliance, idUser, (select max(t.idTick) from tick t) from applications;

drop table applications;

insert into dbPatch values (null, now(), 'enrich alliance application', '0.0.13-1');
