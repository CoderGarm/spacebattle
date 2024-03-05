
alter table rolePlaySetting add column participant varchar(255) not null after firstname;
alter table rolePlaySetting add column winner varchar(255) not null after titleAbbreviation;

# noinspection SqlWithoutWhere
update rolePlaySetting set participant = 'SEASON_2';
update rolePlaySetting set participant = CONCAT('WAR_HARVEST_23|', participant) where idUser in (1, 3, 5, 13, 23, 24, 29);

update rolePlaySetting set winner = 'WAR_HARVEST_23' where idUser = 3;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'add event participations', '0.1.17-6');
