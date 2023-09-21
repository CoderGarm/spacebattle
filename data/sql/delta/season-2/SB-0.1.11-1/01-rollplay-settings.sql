
    create table rolePlaySetting (
       idRolePlaySetting integer not null auto_increment,
        firstname varchar(50),
        shipNameTemplates varchar(255) not null,
        shipNames longtext,
        shipPrefix varchar(6),
        surname varchar(50),
        title varchar(50),
        titleAbbreviation varchar(8),
        idUser integer not null,
        primary key (idRolePlaySetting)
    ) engine=InnoDB;


    alter table rolePlaySetting
       add constraint UK_5sx33g2kpg6lhpamw75ibqb9i unique (idUser);


    alter table rolePlaySetting
       add constraint FKhphq3ivotnm200m2l1h30rej4
       foreign key (idUser)
       references user (idUser);

insert into rolePlaySetting (idUser, shipNameTemplates) select u.idUser, 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE' from user u where u.dType = 'USER';

insert into dbPatch values (null, now(), 'add roleplay data', '0.1.11-1');
