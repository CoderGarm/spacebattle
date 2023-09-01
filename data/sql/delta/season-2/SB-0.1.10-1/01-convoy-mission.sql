
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

insert into dbPatch values (null, now(), 'add convoy mission', '0.1.10-1');
