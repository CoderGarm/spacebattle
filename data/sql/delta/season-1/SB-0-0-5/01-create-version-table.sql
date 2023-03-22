create table dbPatch (
   idDBPatch integer not null auto_increment,
    createdAt datetime(6) not null,
    description varchar(255) not null,
    version varchar(255) not null,
    primary key (idDBPatch)
) engine=InnoDB;

insert into dbPatch values (null, now(), 'create dbPatch table', '0.0.5-1');