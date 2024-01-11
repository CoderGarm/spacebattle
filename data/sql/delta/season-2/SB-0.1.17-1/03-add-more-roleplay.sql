
alter table rolePlaySetting add column empireName varchar(50) after idRolePlaySetting;

INSERT INTO rolePlaySetting (idUser, shipPrefix, empireName, shipNameTemplates) VALUES
(7, 'HMS', 'Star Kingdom of Manticore', 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE'),
(8, 'SLNS', 'Solarian League', 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE'),
(9, 'PNS', 'Haven Republic', 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE'),
(10, 'SMS', 'Anderman Empire', 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE'),
(15, 'OPA', 'Kersey Association', 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE'),
(48, 'SCNS', 'Silesia Confederacy', 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE'),
(49, 'MFS', 'Midgard Federation', 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE'),
(50, 'AAS', 'Asgard Association', 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE'),
(51, 'RTU', 'Rembrandt Trade Union', 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE'),
(52, 'MTA', 'Meroa Trading Association', 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE');

INSERT INTO rolePlaySetting (idUser, shipNameTemplates) VALUES (2, 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE');

alter table rolePlaySetting add column leftBottom longtext after empireName;
alter table rolePlaySetting add column leftUpper longtext after leftBottom;
alter table rolePlaySetting add column rightBottom longtext after leftUpper;
alter table rolePlaySetting add column rightUpper longtext after rightBottom;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'more roleplay', '0.1.17-3');
