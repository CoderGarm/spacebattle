
    create table eventRanking (
       idEventRanking integer not null auto_increment,
        gameEvent varchar(255) not null,
        points integer not null,
        rankingCategory varchar(255) not null,
        idUser integer not null,
        primary key (idEventRanking)
    ) engine=InnoDB;

    alter table eventRanking
       add constraint POINTS_UK unique (idUser, gameEvent, rankingCategory);

    alter table eventRanking
       add constraint FKa03fqyutfi8ul5kr37ce7kra9
       foreign key (idUser)
       references user (idUser);


INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 1, 'GAINED_PLANETS', 1);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 45, 'GAINED_CONSTRUCTION_LEVELS', 1);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 0, 'FLEET_TONNAGE_LOST', 1);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 128, 'FLEET_TONNAGE_DESTROYED', 1);

INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 1, 'GAINED_PLANETS', 23);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 123, 'GAINED_CONSTRUCTION_LEVELS', 23);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 0, 'FLEET_TONNAGE_LOST', 23);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 225, 'FLEET_TONNAGE_DESTROYED', 23);

INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 1, 'GAINED_PLANETS', 29);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 49, 'GAINED_CONSTRUCTION_LEVELS', 29);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 0, 'FLEET_TONNAGE_LOST', 29);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 332, 'FLEET_TONNAGE_DESTROYED', 29);

INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 1, 'GAINED_PLANETS', 24);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 49, 'GAINED_CONSTRUCTION_LEVELS', 24);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 0, 'FLEET_TONNAGE_LOST', 24);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 332, 'FLEET_TONNAGE_DESTROYED', 24);

INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 460, 'FLEET_TONNAGE_LOST', 5);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 161, 'FLEET_TONNAGE_DESTROYED', 5);

INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 388, 'FLEET_TONNAGE_LOST', 13);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 129, 'FLEET_TONNAGE_DESTROYED', 13);

INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 1, 'GAINED_PLANETS', 3);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 123, 'GAINED_CONSTRUCTION_LEVELS', 3);

INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 1718, 'FLEET_TONNAGE_LOST', 3);
INSERT INTO eventRanking (gameEvent, points, rankingCategory, idUser) VALUES ('WAR_HARVEST_23', 1350, 'FLEET_TONNAGE_DESTROYED', 3);

INSERT INTO dbPatch VALUES (NULL, NOW(), 'introduce game events', '0.1.17-2');
