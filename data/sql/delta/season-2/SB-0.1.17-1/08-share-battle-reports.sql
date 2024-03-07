
alter table battleReport add column uuid varchar(255) not null after lastRound;

# noinspection SqlWithoutWhere
update battleReport set uuid = SUBSTRING_INDEX(uuid(), '-', 1);

    create table sharedBattleReport (
       idSharedBattleReport integer not null auto_increment,
        shareWithEveryone boolean not null default false,
        idBattleReport integer not null,
        primary key (idSharedBattleReport)
    ) engine=InnoDB;

insert into sharedBattleReport (idSharedBattleReport, shareWithEveryone, idBattleReport) select null, false, idBattleReport from battleReport;

    create table participatingUsers_TEMP (
           idBattleReport integer not null,
            idUser integer not null,
            primary key (idBattleReport, idUser)
        ) engine=InnoDB;

insert into participatingUsers_TEMP (idBattleReport, idUser) select idBattleReport, idUser from participatingUsers;

    create table sharedWithUsers (
       idSharedBattleReport integer not null,
        idUser integer not null,
        primary key (idSharedBattleReport, idUser)
    ) engine=InnoDB;

    alter table sharedBattleReport
       add constraint UK_hpe1gr8wmca659sm228uhgp6u unique (idBattleReport);

    alter table sharedBattleReport
       add constraint FKyk7jxlttaktlvdhc231rwt80
       foreign key (idBattleReport)
       references battleReport (idBattleReport);

    create table sharedWithAlliances (
       idSharedBattleReport integer not null,
        idAlliance integer not null,
        primary key (idSharedBattleReport, idAlliance)
    ) engine=InnoDB;


    alter table sharedWithAlliances
       add constraint FK3tp6tcx1ulbyfxa69lhh7ad1e
       foreign key (idAlliance)
       references alliance (idAlliance);

    alter table sharedWithAlliances
       add constraint FKn7nuh81nwugcay3yigdhmaqva
       foreign key (idSharedBattleReport)
       references sharedBattleReport (idSharedBattleReport);

    alter table sharedWithUsers
       add constraint FK3wt2lys7aqxisvvcxjhkh2iit
       foreign key (idUser)
       references user (idUser);

    alter table sharedWithUsers
       add constraint FKm9oj0tt8rij6u5r5mmfaexlwn
       foreign key (idSharedBattleReport)
       references sharedBattleReport (idSharedBattleReport);

drop table participatingUsers;

    create table participatingUsers (
       idSharedBattleReport integer not null,
        idUser integer not null,
        primary key (idSharedBattleReport, idUser)
    ) engine=InnoDB;

insert into participatingUsers (idSharedBattleReport, idUser) select (select idSharedBattleReport from sharedBattleReport where idBattleReport = p.idBattleReport), idUser from participatingUsers_TEMP p;

    alter table participatingUsers
           add constraint FK5cqqxdpvyx4jmlpmu1khk00sv
           foreign key (idSharedBattleReport)
           references sharedBattleReport (idSharedBattleReport);

drop table participatingUsers_TEMP;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'share battle reports', '0.1.17-8');
