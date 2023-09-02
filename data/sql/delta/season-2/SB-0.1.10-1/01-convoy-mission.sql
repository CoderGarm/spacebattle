
alter table mission drop constraint FKholrjg4864rt9j8qqs349b7ue;
alter table mission modify column idPlanet integer;
alter table mission
    add constraint FKholrjg4864rt9j8qqs349b7ue
    foreign key (idPlanet) references planet (idPlanet);

alter table mission add column idTradeResource integer after idTickStoppedAt;
alter table mission
    add constraint FKgn39ow7ddmkf4bhyk50s47m1f
    foreign key (idTradeResource)
    references tradedResource (idTradedResource);

create table missionItem (
   missionType varchar(31) not null,
    idMissionItem integer not null auto_increment,
    phase varchar(255),
    isRansomPayment bit,
    percentOfCargoLost integer,
    piratedWithdraw bit,
    piratedWithdrawAfterApproach bit,
    idTickCreatedAt integer not null,
    idTradeResource integer,
    primary key (idMissionItem)
) engine=InnoDB;

alter table missionItem
   add constraint FKjw6nl0yyik5mtyv227litad57
   foreign key (idTickCreatedAt)
   references tick (idTick);

alter table missionItem
   add constraint FKpiatw8caol0quww65cxgqeayc
   foreign key (idTradeResource)
   references tradedResource (idTradedResource);

insert into dbPatch values (null, now(), 'add convoy mission', '0.1.10-1');
