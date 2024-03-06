

    create table sharedBattleReport (
       idSharedBattleReport integer not null auto_increment,
        shareWithEveryone boolean not null default false,
        idAlliance integer,
        idBattleReport integer not null,
        primary key (idSharedBattleReport)
    ) engine=InnoDB;

insert into sharedBattleReport (idSharedBattleReport, shareWithEveryone, idAlliance, idBattleReport) select null, false, null, idBattleReport from battleReport;

    create table sharedWithUsers (
       idSharedBattleReport integer not null,
        idUser integer not null,
        primary key (idSharedBattleReport, idUser)
    ) engine=InnoDB;

    alter table sharedBattleReport
       add constraint UK_hpe1gr8wmca659sm228uhgp6u unique (idBattleReport);

    alter table sharedBattleReport
       add constraint FK7tseyh70o5cbaenxmog7ao3l9
       foreign key (idAlliance)
       references alliance (idAlliance);

    alter table sharedBattleReport
       add constraint FKyk7jxlttaktlvdhc231rwt80
       foreign key (idBattleReport)
       references battleReport (idBattleReport);

    alter table sharedWithUsers
       add constraint FK3wt2lys7aqxisvvcxjhkh2iit
       foreign key (idUser)
       references user (idUser);

    alter table sharedWithUsers
       add constraint FKm9oj0tt8rij6u5r5mmfaexlwn
       foreign key (idSharedBattleReport)
       references sharedBattleReport (idSharedBattleReport);

INSERT INTO dbPatch VALUES (NULL, NOW(), 'share battle reports', '0.1.17-8');
