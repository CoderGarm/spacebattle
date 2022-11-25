
alter table planet add column idResourceTransportationDelivery integer after idResourceUtilization;
alter table planet add column idResourceTransportationDemand integer after idResourceTransportationDelivery;


DELIMITER |
CREATE PROCEDURE insertResourceStuff()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM planet INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select p.idPlanet from planet p offset i rows fetch next 1 row only into @idPlanet;

            insert into resourceDeposit values (null, 'TRANSPORTATION_DEMAND');
            select LAST_INSERT_ID() into @idR;
            update planet set idResourceTransportationDemand = @idR where idPlanet = @idPlanet;

            insert into resourceDeposit values (null, 'TRANSPORTATION_DELIVERY');
            select LAST_INSERT_ID() into @idR;
            update planet set idResourceTransportationDelivery = @idR where idPlanet = @idPlanet;

            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertResourceStuff();
drop procedure insertResourceStuff;

alter table planet
   add constraint FK7ica1a9s7r5jy3dn3krk4hkfe
   foreign key (idResourceTransportationDelivery)
   references resourceDeposit (idResourceDeposit);

alter table planet
   add constraint FKipb2odgmfpbftjlah8gxjh6fw
   foreign key (idResourceTransportationDemand)
   references resourceDeposit (idResourceDeposit);

INSERT INTO article (langCode, title, wikiCategory, idBase) VALUES ('en', 'Transport and Migration', 'GAME_MECHANICS', null);

INSERT INTO articleRevision (version, idArticle, idAuthor) VALUES (1, 5, 1);

INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, '# Transport and Migration', 'INSERT', 0);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, '---', 'INSERT', 1);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, '## Transport', 'INSERT', 2);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, '', 'INSERT', 3);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, 'For every planet it can be set what is allowed to be delivered and what is needed.  ', 'INSERT', 4);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, 'If a delivery could really be done depends on the availability of the demanded resources or military personnel.', 'INSERT', 5);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, '', 'INSERT', 6);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, 'The transport companies of your empire will execute the jobs, searching the best routes and deliver at the time.', 'INSERT', 7);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, '', 'INSERT', 8);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, '---', 'INSERT', 9);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, 'For obvious reasons you can command your military, but not your civilians.  ', 'INSERT', 10);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (5, 'They will detect opportunities on other planets on their own and will move to planets with better working conditions.', 'INSERT', 11);

INSERT INTO article_articleRevisions (Article_idArticle, articleRevisions_idArticleRevision) VALUES (5, 5);

insert into dbPatch values (null, now(), 'add transportation', '0.0.7-1');