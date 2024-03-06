
alter table fleet add column idTickActivated integer after yCoordinateLocation;
alter table orbitalStructure add column idTickActivated integer after yCoordinate;
alter table warShip add column idTickActivated integer after name;
alter table warshipHealthStateSnapshot add column idTickActivated integer after isFightingCapable;

update fleet set idTickActivated = 1 where isOperational = true;
update orbitalStructure set idTickActivated = 1 where isOperational = true;
update warShip set idTickActivated = 1 where isOperational = true;
update warshipHealthStateSnapshot set idTickActivated = 1 where isOperational = true;

    alter table fleet
       add constraint FKkv8p42ny0lnkcqrrsvrpltsno
       foreign key (idTickActivated)
       references tick (idTick);

    alter table orbitalStructure
       add constraint FKcnjc3xew6n67k9q8qhwkntbcb
       foreign key (idTickActivated)
       references tick (idTick);

    alter table warShip
       add constraint FKm26l3odxqppbhlowov3tc64y9
       foreign key (idTickActivated)
       references tick (idTick);

    alter table warshipHealthStateSnapshot
       add constraint FK11aatx339r9rwxxawx31i5ub1
       foreign key (idTickActivated)
       references tick (idTick);

alter table construction add column isOperational boolean not null default false after idConstruction;
alter table construction add column idTickActivated integer after operationalLevel;
update construction set idTickActivated = 1 where isOperational = true;

    alter table construction
       add constraint FKah9i54pwsqd526a91s3ryci5o
       foreign key (idTickActivated)
       references tick (idTick);

INSERT INTO dbPatch VALUES (NULL, NOW(), 'replace operational cache', '0.1.17-7');
