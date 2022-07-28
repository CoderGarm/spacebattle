#---------------------- create columns

alter table ammunitionModule
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table armor
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table building
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table electronicWarfare
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table hull
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table launcher
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table missileMotor
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table passiveModule
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table propulsion
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table research
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table sidewall
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table warhead
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;

alter table weapon
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;


alter table missile
    add column idTranslatableDescription integer not null after idCosts,
    add column idTranslatableName        integer not null after idTranslatableDescription;


create table translatable
(
    idTranslatable    integer      not null auto_increment,
    idParent          integer      not null,
    translatableType  varchar(255) not null,
    translationTarget varchar(255) not null,
    primary key (idTranslatable)
) engine = InnoDB;

create table translation
(
    idTranslation integer not null auto_increment,
    languageCode  varchar(2),
    translation   varchar(255),
    primary key (idTranslation)
) engine = InnoDB;

create table translationCollection
(
    idTranslatable integer not null,
    idTranslation  integer not null
) engine = InnoDB;

#---------------------- transfer values
#select * from translation; select * from translatable; select * from translationCollection; select * from ammunitionModule;

# ammunitionModule
DELIMITER |
CREATE PROCEDURE insertTranslatableAmmunitionModuleNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM ammunitionModule where ammunitionModule.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idAmmunitionModule from ammunitionModule a order by idAmmunitionModule offset i rows fetch next 1 row only into @idAmmunitionModule;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idAmmunitionModule, 'AMMUNITION_MODULE', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idAmmunitionModule
            from ammunitionModule
            where ammunitionModule.idTranslatableName = 0
            limit 1
            into @idAmmunitionModule;
            update ammunitionModule
            SET ammunitionModule.idTranslatableName = @idTranslatable
            where idAmmunitionModule = @idAmmunitionModule;
            insert into translation (languageCode, translation)
            select 'en', name
            from ammunitionModule
            where idAmmunitionModule = @idAmmunitionModule;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableAmmunitionModuleNames();
drop procedure insertTranslatableAmmunitionModuleNames;

DELIMITER |
CREATE PROCEDURE insertTranslatableAmmunitionModuleDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM ammunitionModule where ammunitionModule.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idAmmunitionModule from ammunitionModule a order by idAmmunitionModule offset i rows fetch next 1 row only into @idAmmunitionModule;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idAmmunitionModule, 'AMMUNITION_MODULE', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idAmmunitionModule
            from ammunitionModule
            where ammunitionModule.idTranslatableDescription = 0
            limit 1
            into @idAmmunitionModule;
            update ammunitionModule
            SET ammunitionModule.idTranslatableDescription = @idTranslatable
            where idAmmunitionModule = @idAmmunitionModule;
            insert into translation (languageCode, translation)
            select 'en', description
            from ammunitionModule
            where idAmmunitionModule = @idAmmunitionModule;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableAmmunitionModuleDesc();
drop procedure insertTranslatableAmmunitionModuleDesc;


#armor
DELIMITER |
CREATE PROCEDURE insertTranslatableArmorNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM armor where armor.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idArmor from armor a order by idArmor offset i rows fetch next 1 row only into @idArmor;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idArmor, 'ARMOR', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idArmor from armor where armor.idTranslatableName = 0 limit 1 into @idArmor;
            update armor SET armor.idTranslatableName = @idTranslatable where idArmor = @idArmor;
            insert into translation (languageCode, translation) select 'en', name from armor where idArmor = @idArmor;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;


call insertTranslatableArmorNames();
drop procedure insertTranslatableArmorNames;

DELIMITER |
CREATE PROCEDURE insertTranslatableArmorDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM armor where armor.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idArmor from armor a order by idArmor offset i rows fetch next 1 row only into @idArmor;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idArmor, 'ARMOR', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idArmor from armor where armor.idTranslatableDescription = 0 limit 1 into @idArmor;
            update armor SET armor.idTranslatableDescription = @idTranslatable where idArmor = @idArmor;
            insert into translation (languageCode, translation)
            select 'en', description
            from armor
            where idArmor = @idArmor;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableArmorDesc();
drop procedure insertTranslatableArmorDesc;


# building
DELIMITER |
CREATE PROCEDURE insertTranslatableBuildingNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM building where building.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idBuilding from building a order by idBuilding offset i rows fetch next 1 row only into @idBuilding;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idBuilding, 'BUILDING', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idBuilding from building where building.idTranslatableName = 0 limit 1 into @idBuilding;
            update building SET building.idTranslatableName = @idTranslatable where idBuilding = @idBuilding;
            insert into translation (languageCode, translation)
            select 'en', name
            from building
            where idBuilding = @idBuilding;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableBuildingNames();
drop procedure insertTranslatableBuildingNames;

DELIMITER |
CREATE PROCEDURE insertTranslatableBuildingDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM building where building.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idBuilding from building a order by idBuilding offset i rows fetch next 1 row only into @idBuilding;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idBuilding, 'BUILDING', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idBuilding from building where building.idTranslatableDescription = 0 limit 1 into @idBuilding;
            update building SET building.idTranslatableDescription = @idTranslatable where idBuilding = @idBuilding;
            insert into translation (languageCode, translation)
            select 'en', description
            from building
            where idBuilding = @idBuilding;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableBuildingDesc();
drop procedure insertTranslatableBuildingDesc;


# electronicWarfare
DELIMITER |
CREATE PROCEDURE insertTranslatableElectronicWarfareNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM electronicWarfare where electronicWarfare.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idElectronicWarfare from electronicWarfare a order by idElectronicWarfare offset i rows fetch next 1 row only into @idElectronicWarfare;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idElectronicWarfare, 'ELECTRONIC_WARFARE', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idElectronicWarfare
            from electronicWarfare
            where electronicWarfare.idTranslatableName = 0
            limit 1
            into @idElectronicWarfare;
            update electronicWarfare
            SET electronicWarfare.idTranslatableName = @idTranslatable
            where idElectronicWarfare = @idElectronicWarfare;
            insert into translation (languageCode, translation)
            select 'en', name
            from electronicWarfare
            where idElectronicWarfare = @idElectronicWarfare;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableElectronicWarfareNames();
drop procedure insertTranslatableElectronicWarfareNames;

DELIMITER |
CREATE PROCEDURE insertTranslatableElectronicWarfareDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM electronicWarfare where electronicWarfare.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idElectronicWarfare from electronicWarfare a order by idElectronicWarfare offset i rows fetch next 1 row only into @idElectronicWarfare;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idElectronicWarfare, 'ELECTRONIC_WARFARE', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idElectronicWarfare
            from electronicWarfare
            where electronicWarfare.idTranslatableDescription = 0
            limit 1
            into @idElectronicWarfare;
            update electronicWarfare
            SET electronicWarfare.idTranslatableDescription = @idTranslatable
            where idElectronicWarfare = @idElectronicWarfare;
            insert into translation (languageCode, translation)
            select 'en', description
            from electronicWarfare
            where idElectronicWarfare = @idElectronicWarfare;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableElectronicWarfareDesc();
drop procedure insertTranslatableElectronicWarfareDesc;


# hull
DELIMITER |
CREATE PROCEDURE insertTranslatableHullNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM hull where hull.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idHull from hull a order by idHull offset i rows fetch next 1 row only into @idHull;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idHull, 'HULL', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idHull from hull where hull.idTranslatableName = 0 limit 1 into @idHull;
            update hull SET hull.idTranslatableName = @idTranslatable where idHull = @idHull;
            insert into translation (languageCode, translation) select 'en', name from hull where idHull = @idHull;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableHullNames();
drop procedure insertTranslatableHullNames;

DELIMITER |
CREATE PROCEDURE insertTranslatableHullDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM hull where hull.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idHull from hull a order by idHull offset i rows fetch next 1 row only into @idHull;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idHull, 'HULL', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idHull from hull where hull.idTranslatableDescription = 0 limit 1 into @idHull;
            update hull SET hull.idTranslatableDescription = @idTranslatable where idHull = @idHull;
            insert into translation (languageCode, translation)
            select 'en', description
            from hull
            where idHull = @idHull;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableHullDesc();
drop procedure insertTranslatableHullDesc;


# launcher
DELIMITER |
CREATE PROCEDURE insertTranslatableLauncherNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM launcher where launcher.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idLauncher from launcher a order by idLauncher offset i rows fetch next 1 row only into @idLauncher;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idLauncher, 'LAUNCHER', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idLauncher from launcher where launcher.idTranslatableName = 0 limit 1 into @idLauncher;
            update launcher SET launcher.idTranslatableName = @idTranslatable where idLauncher = @idLauncher;
            insert into translation (languageCode, translation)
            select 'en', name
            from launcher
            where idLauncher = @idLauncher;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableLauncherNames();
drop procedure insertTranslatableLauncherNames;

DELIMITER |
CREATE PROCEDURE insertTranslatableLauncherDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM launcher where launcher.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idLauncher from launcher a order by idLauncher offset i rows fetch next 1 row only into @idLauncher;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idLauncher, 'LAUNCHER', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idLauncher from launcher where launcher.idTranslatableDescription = 0 limit 1 into @idLauncher;
            update launcher SET launcher.idTranslatableDescription = @idTranslatable where idLauncher = @idLauncher;
            insert into translation (languageCode, translation)
            select 'en', description
            from launcher
            where idLauncher = @idLauncher;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableLauncherDesc();
drop procedure insertTranslatableLauncherDesc;


# passiveModule
DELIMITER |
CREATE PROCEDURE insertTranslatablePassiveModuleNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM passiveModule where passiveModule.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idPassiveModule from passiveModule a order by idPassiveModule offset i rows fetch next 1 row only into @idPassiveModule;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idPassiveModule, 'PASSIVE_MODULE', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idPassiveModule
            from passiveModule
            where passiveModule.idTranslatableName = 0
            limit 1
            into @idPassiveModule;
            update passiveModule
            SET passiveModule.idTranslatableName = @idTranslatable
            where idPassiveModule = @idPassiveModule;
            insert into translation (languageCode, translation)
            select 'en', name
            from passiveModule
            where idPassiveModule = @idPassiveModule;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatablePassiveModuleNames();
drop procedure insertTranslatablePassiveModuleNames;

DELIMITER |
CREATE PROCEDURE insertTranslatablePassiveModuleDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM passiveModule where passiveModule.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idPassiveModule from passiveModule a order by idPassiveModule offset i rows fetch next 1 row only into @idPassiveModule;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idPassiveModule, 'PASSIVE_MODULE', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idPassiveModule
            from passiveModule
            where passiveModule.idTranslatableDescription = 0
            limit 1
            into @idPassiveModule;
            update passiveModule
            SET passiveModule.idTranslatableDescription = @idTranslatable
            where idPassiveModule = @idPassiveModule;
            insert into translation (languageCode, translation)
            select 'en', description
            from passiveModule
            where idPassiveModule = @idPassiveModule;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatablePassiveModuleDesc();
drop procedure insertTranslatablePassiveModuleDesc;


# propulsion
DELIMITER |
CREATE PROCEDURE insertTranslatablePropulsionNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM propulsion where propulsion.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idPropulsion from propulsion a order by idPropulsion offset i rows fetch next 1 row only into @idPropulsion;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idPropulsion, 'PROPULSION', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idPropulsion from propulsion where propulsion.idTranslatableName = 0 limit 1 into @idPropulsion;
            update propulsion SET propulsion.idTranslatableName = @idTranslatable where idPropulsion = @idPropulsion;
            insert into translation (languageCode, translation)
            select 'en', name
            from propulsion
            where idPropulsion = @idPropulsion;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatablePropulsionNames();
drop procedure insertTranslatablePropulsionNames;

DELIMITER |
CREATE PROCEDURE insertTranslatablePropulsionDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM propulsion where propulsion.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idPropulsion from propulsion a order by idPropulsion offset i rows fetch next 1 row only into @idPropulsion;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idPropulsion, 'PROPULSION', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idPropulsion
            from propulsion
            where propulsion.idTranslatableDescription = 0
            limit 1
            into @idPropulsion;
            update propulsion
            SET propulsion.idTranslatableDescription = @idTranslatable
            where idPropulsion = @idPropulsion;
            insert into translation (languageCode, translation)
            select 'en', description
            from propulsion
            where idPropulsion = @idPropulsion;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatablePropulsionDesc();
drop procedure insertTranslatablePropulsionDesc;


# research
DELIMITER |
CREATE PROCEDURE insertTranslatableResearchNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM research where research.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idResearch from research a order by idResearch offset i rows fetch next 1 row only into @idResearch;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idResearch, 'RESEARCH', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idResearch from research where research.idTranslatableName = 0 limit 1 into @idResearch;
            update research SET research.idTranslatableName = @idTranslatable where idResearch = @idResearch;
            insert into translation (languageCode, translation)
            select 'en', name
            from research
            where idResearch = @idResearch;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableResearchNames();
drop procedure insertTranslatableResearchNames;

DELIMITER |
CREATE PROCEDURE insertTranslatableResearchDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM research where research.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idResearch from research a order by idResearch offset i rows fetch next 1 row only into @idResearch;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idResearch, 'RESEARCH', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idResearch from research where research.idTranslatableDescription = 0 limit 1 into @idResearch;
            update research SET research.idTranslatableDescription = @idTranslatable where idResearch = @idResearch;
            insert into translation (languageCode, translation)
            select 'en', description
            from research
            where idResearch = @idResearch;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableResearchDesc();
drop procedure insertTranslatableResearchDesc;


# sidewall
DELIMITER |
CREATE PROCEDURE insertTranslatableSidewallNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM sidewall where sidewall.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idSidewall from sidewall a order by idSidewall offset i rows fetch next 1 row only into @idSidewall;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idSidewall, 'SIDEWALL', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idSidewall from sidewall where sidewall.idTranslatableName = 0 limit 1 into @idSidewall;
            update sidewall SET sidewall.idTranslatableName = @idTranslatable where idSidewall = @idSidewall;
            insert into translation (languageCode, translation)
            select 'en', name
            from sidewall
            where idSidewall = @idSidewall;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableSidewallNames();
drop procedure insertTranslatableSidewallNames;

DELIMITER |
CREATE PROCEDURE insertTranslatableSidewallDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM sidewall where sidewall.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idSidewall from sidewall a order by idSidewall offset i rows fetch next 1 row only into @idSidewall;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idSidewall, 'SIDEWALL', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idSidewall from sidewall where sidewall.idTranslatableDescription = 0 limit 1 into @idSidewall;
            update sidewall SET sidewall.idTranslatableDescription = @idTranslatable where idSidewall = @idSidewall;
            insert into translation (languageCode, translation)
            select 'en', description
            from sidewall
            where idSidewall = @idSidewall;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableSidewallDesc();
drop procedure insertTranslatableSidewallDesc;

# weapon
DELIMITER |
CREATE PROCEDURE insertTranslatableWeaponNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM weapon where weapon.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idWeapon from weapon a order by idWeapon offset i rows fetch next 1 row only into @idWeapon;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idWeapon, 'WEAPON', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idWeapon from weapon where weapon.idTranslatableName = 0 limit 1 into @idWeapon;
            update weapon SET weapon.idTranslatableName = @idTranslatable where idWeapon = @idWeapon;
            insert into translation (languageCode, translation)
            select 'en', name
            from weapon
            where idWeapon = @idWeapon;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableWeaponNames();
drop procedure insertTranslatableWeaponNames;

DELIMITER |
CREATE PROCEDURE insertTranslatableWeaponDesc()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM weapon where weapon.idTranslatableDescription = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idWeapon from weapon a order by idWeapon offset i rows fetch next 1 row only into @idWeapon;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idWeapon, 'WEAPON', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            select idWeapon from weapon where weapon.idTranslatableDescription = 0 limit 1 into @idWeapon;
            update weapon SET weapon.idTranslatableDescription = @idTranslatable where idWeapon = @idWeapon;
            insert into translation (languageCode, translation)
            select 'en', description
            from weapon
            where idWeapon = @idWeapon;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableWeaponDesc();
drop procedure insertTranslatableWeaponDesc;

#typeName only
#warhead
DELIMITER |
CREATE PROCEDURE insertTranslatableWarheadNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM warhead where warhead.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idWarhead from warhead a order by idWarhead offset i rows fetch next 1 row only into @idWarhead;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idWarhead, 'WARHEAD', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idWarhead from warhead where warhead.idTranslatableName = 0 limit 1 into @idWarhead;
            update warhead SET warhead.idTranslatableName = @idTranslatable where idWarhead = @idWarhead;
            insert into translation (languageCode, translation)
            select 'en', typeName
            from warhead
            where idWarhead = @idWarhead;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);

            #create description
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idWarhead, 'WARHEAD', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            update warhead SET warhead.idTranslatableDescription = @idTranslatable where idWarhead = @idWarhead;
            insert into translation (languageCode, translation)
            select 'en', typeName
            from warhead
            where idWarhead = @idWarhead;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableWarheadNames();
drop procedure insertTranslatableWarheadNames;

#missileMotor
DELIMITER |
CREATE PROCEDURE insertTranslatableMissileMotorNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM missileMotor where missileMotor.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idMissileMotor from missileMotor a order by idMissileMotor offset i rows fetch next 1 row only into @idMissileMotor;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idMissileMotor, 'MISSILE_MOTOR', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idMissileMotor
            from missileMotor
            where missileMotor.idTranslatableName = 0
            limit 1
            into @idMissileMotor;
            update missileMotor
            SET missileMotor.idTranslatableName = @idTranslatable
            where idMissileMotor = @idMissileMotor;
            insert into translation (languageCode, translation)
            select 'en', typeName
            from missileMotor
            where idMissileMotor = @idMissileMotor;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);

            #create description
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idMissileMotor, 'MISSILE_MOTOR', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            update missileMotor SET missileMotor.idTranslatableDescription = @idTranslatable where idMissileMotor = @idMissileMotor;
            insert into translation (languageCode, translation)
            select 'en', typeName
            from missileMotor
            where idMissileMotor = @idMissileMotor;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableMissileMotorNames();
drop procedure insertTranslatableMissileMotorNames;

#missile
DELIMITER |
CREATE PROCEDURE insertTranslatableMissileNames()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM missile where missile.idTranslatableName = 0 INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select a.idMissile from missile a order by idMissile offset i rows fetch next 1 row only into @idMissile;
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idMissile, 'MISSILE', 'NAME');
            select LAST_INSERT_ID() into @idTranslatable;
            select idMissile from missile where missile.idTranslatableName = 0 limit 1 into @idMissile;
            update missile SET missile.idTranslatableName = @idTranslatable where idMissile = @idMissile;
            insert into translation (languageCode, translation)
            select 'en', typeName
            from missile
            where idMissile = @idMissile;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);

            #create description
            insert into translatable (idTranslatable, idParent, translationTarget, translatableType) values (null, @idMissile, 'MISSILE', 'DESCRIPTION');
            select LAST_INSERT_ID() into @idTranslatable;
            update missile SET missile.idTranslatableDescription = @idTranslatable where idMissile = @idMissile;
            insert into translation (languageCode, translation)
            select 'en', typeName
            from missile
            where idMissile = @idMissile;
            select max(idTranslation) from translation into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertTranslatableMissileNames();
drop procedure insertTranslatableMissileNames;


#---------------------- set foreign keys


alter table translationCollection
    add constraint UK_dd7cu1at8xry52twepp0lxcw8 unique (idTranslation);
alter table ammunitionModule
    add constraint FKivsjmyi7f7aym46q08qh71k1i foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table ammunitionModule
    add constraint FK9jn8385qcftsln9aiemohys6x foreign key (idTranslatableName) references translatable (idTranslatable);
alter table armor
    add constraint FK843wjkvvkloflykng3p5xanqf foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table armor
    add constraint FKj95rgrpe0e0kldkrdk61180vb foreign key (idTranslatableName) references translatable (idTranslatable);
alter table building
    add constraint FK9jureiokh5eus3dq46euhltxo foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table building
    add constraint FKmqi7vubpnykxhu53hy5e7qri2 foreign key (idTranslatableName) references translatable (idTranslatable);
alter table electronicWarfare
    add constraint FKi180vaq7gab8jy3r99bawdic5 foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table electronicWarfare
    add constraint FKgehovt9s2xat817l0enflo5nq foreign key (idTranslatableName) references translatable (idTranslatable);
alter table hull
    add constraint FKi1ghgbbrc1j4vovj7v03t0sd5 foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table hull
    add constraint FK7g5aas0xko5stotsvyt9hhchw foreign key (idTranslatableName) references translatable (idTranslatable);
alter table launcher
    add constraint FKn80gj1fyhvn6v5smkbx3b4rhi foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table launcher
    add constraint FKa0tf8xicyfrn906krw65ieop1 foreign key (idTranslatableName) references translatable (idTranslatable);
alter table missile
    add constraint FK3sugkdm5phqm3kkdraprgaj87 foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table missile
    add constraint FKorhea214ty529liubde204v2y foreign key (idTranslatableName) references translatable (idTranslatable);
alter table missileMotor
    add constraint FKs8aryxvu0dbr41yab1lqy7f54 foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table missileMotor
    add constraint FKkygcap68itcbqkfukqxqhqti8 foreign key (idTranslatableName) references translatable (idTranslatable);
alter table passiveModule
    add constraint FK3q0uitju15ai7lhv7y7y61549 foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table passiveModule
    add constraint FK1kqbngjlngfx049m4t1hmyelt foreign key (idTranslatableName) references translatable (idTranslatable);
alter table propulsion
    add constraint FK1a2sbiyhyhlm5q99g8cs8qdpw foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table propulsion
    add constraint FK34jwo45015kmmtttnorlypaa3 foreign key (idTranslatableName) references translatable (idTranslatable);
alter table research
    add constraint FK1sxfrsxvrj2iaxi809oirhevj foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table research
    add constraint FKibqicobq7dm63vf792kgmk5wj foreign key (idTranslatableName) references translatable (idTranslatable);
alter table sidewall
    add constraint FKdx39gsmusm1sai6wdid4s4xmn foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table sidewall
    add constraint FKmqieo3lwi46pddbgbhg7dbg4r foreign key (idTranslatableName) references translatable (idTranslatable);
alter table translationCollection
    add constraint FKgrdmwu4xhrjpi4i9oof3ob2gt foreign key (idTranslation) references translation (idTranslation);
alter table translationCollection
    add constraint FKeytw64w0mw10fundphgc4e5f6 foreign key (idTranslatable) references translatable (idTranslatable);
alter table warhead
    add constraint FKjx1sqa9iiinbgfciltxdqp78u foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table warhead
    add constraint FKa939x3f6pjibpdv9k0wxbl3cq foreign key (idTranslatableName) references translatable (idTranslatable);
alter table weapon
    add constraint FKqx172dx6j907oe0gcxskan5vy foreign key (idTranslatableDescription) references translatable (idTranslatable);
alter table weapon
    add constraint FKtrgd2x03dkumgxnryvhon8qm5 foreign key (idTranslatableName) references translatable (idTranslatable);

#---------------------- drop columns

alter table ammunitionModule
    drop column description,
    drop column name;
alter table armor
    drop column description,
    drop column name;
alter table building
    drop column description,
    drop column name;
alter table electronicWarfare
    drop column description,
    drop column name;
alter table hull
    drop column description,
    drop column name;
alter table launcher
    drop column description,
    drop column name;
alter table passiveModule
    drop column description,
    drop column name;

alter table propulsion
    drop column description,
    drop column name;
alter table research
    drop column description,
    drop column name;
alter table sidewall
    drop column description,
    drop column name;
alter table weapon
    drop column description,
    drop column name;
alter table missileMotor
    drop column typeName;
alter table warhead
    drop column typeName;
alter table missile
    drop column typeName;

#---------------------amend german translations


DELIMITER |
CREATE PROCEDURE insertGermanTranslations()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM translation INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select t.idTranslation, t.translation from translation t order by t.idTranslation offset i rows fetch next 1 row only into @idTranslation, @translation;
            select tc.idTranslatable from translationCollection tc where tc.idTranslation = @idTranslation into @idTranslatable;

            #@formatter:off
            if @translation ='Rocket Ammunition' then
                select 'Raketenmunition' into @german;
            elseif @translation = 'Counter Rocket Ammunition' then
                select 'Gegenraketenmunition' into @german;
            elseif @translation =        'A bunch of rockets.' then
                select 'Ein Haufen Raketen.' into @german;
            elseif @translation =        'Another bunch of rockets.' then
                select 'Ein weiterer Haufen Raketen.' into @german;
            elseif @translation =        'Armor Mk I' then
                select 'Rüstung Mk I' into @german;
            elseif @translation =        'An armor' then
                select 'Eine Rüstung' into @german;
            elseif @translation =        'Construction Yard' then
                select 'Bauhof' into @german;
            elseif @translation =        'Orbitals Construction Yard' then
                select 'Orbital Bauhof' into @german;
            elseif @translation =        'Research Laboratories' then
                select 'Forschungslabore' into @german;
            elseif @translation =        'Market place' then
                select 'Marktplatz' into @german;
            elseif @translation =        'Metal works' then
                select 'Metallbearbeitung' into @german;
            elseif @translation =        'Special orbital ores' then
                select 'Spezielle orbitale Erze' into @german;
            elseif @translation =        'Asynchronous Investigations' then
                select 'Asynchrone Ermittlungen' into @german;
            elseif @translation =        'Living room' then
                select 'Wohnzimmer' into @german;
            elseif @translation =        'Hospital' then
                select 'Krankenhaus' into @german;
            elseif @translation =        'Elementary schools' then
                select 'Grundschulen' into @german;
            elseif @translation =        'Secondary schools' then
                select 'Weiterführende Schulen' into @german;
            elseif @translation =        'University' then
                select 'Universität' into @german;
            elseif @translation =        'Teams Rank School' then
                select 'Mannschaftsschule' into @german;
            elseif @translation =        'Military Academy' then
                select 'Militärakademie' into @german;
            elseif @translation =        'The construction yard construct constructions.' then
                select 'Der Bauhof baut Bauwerke.' into @german;
            elseif @translation =        'The construction yard construct orbital constructions.' then
                select 'Der Bauhof baut orbitale Konstruktionen.' into @german;
            elseif @translation =        'The lab investigates researches.' then
                select 'Das Labor untersucht Forschungen.' into @german;
            elseif @translation =        'The market makes money.' then
                select 'Der Markt verdient Geld.' into @german;
            elseif @translation =        'Metals for progress.' then
                select 'Metalle für den Fortschritt.' into @german;
            elseif @translation =        'Heavier metals for more progress.' then
                select 'Schwerere Metalle für mehr Fortschritt.' into @german;
            elseif @translation =        'Rare elements for the future.' then
                select 'Seltene Elemente für die Zukunft.' into @german;
            elseif @translation =        'Everyone needs a home' then
                select 'Jeder braucht ein Zuhause' into @german;
            elseif @translation =        'Everyone needs a doctor' then
                select 'Jeder braucht einen Arzt' into @german;
            elseif @translation =        'a school' then
                select 'eine Schule' into @german;
            elseif @translation =        'another school' then
                select 'eine andere Schule' into @german;
            elseif @translation =        'a university' then
                select 'eine Universität' into @german;
            elseif @translation =        'for the guys which are loud' then
                select 'für die Jungs, die laut sind' into @german;
            elseif @translation =        'for the guys which are silent' then
                select 'für die Jungs, die schweigen' into @german;
            elseif @translation =        'Scanner Mk I' then
                select 'Scanner Mk I' into @german;
            elseif @translation =        'A scanner' then
                select 'Ein Scanner' into @german;
            elseif @translation =        'Corvette vessel' then
                select 'Korvettenschiff' into @german;
            elseif @translation =        'Frigate vessel' then
                select 'Fregattenschiff' into @german;
            elseif @translation =        'Cruiser vessel' then
                select 'Kreuzerschiff' into @german;
            elseif @translation =        'The corvette hull' then
                select 'Der Korvettenrumpf' into @german;
            elseif @translation =        'The frigate hull' then
                select 'Der Fregattenrumpf' into @german;
            elseif @translation =        'The cruiser hull' then
                select 'Der Rumpf des Kreuzers' into @german;
            elseif @translation =        'Ship killer launcher Mk I' then
                select 'Schiffskillerwerfer Mk I' into @german;
            elseif @translation =        'Counter missile launcher Mk I' then
                select 'Abwehrraketenwerfer Mk I' into @german;
            elseif @translation =        'The launcher for ship killers' then
                select 'Der Werfer für Schiffskiller' into @german;
            elseif @translation =        'The launcher for counter missiles' then
                select 'Der Werfer für Gegenraketen' into @german;
            elseif @translation =        'Improves armor' then
                select 'Verbessert die Rüstung' into @german;
            elseif @translation =        'Increases the amount of armor' then
                select 'Erhöht die Menge an Rüstung' into @german;
            elseif @translation =        'Speed Mk I' then
                select 'Geschwindigkeit Mk I' into @german;
            elseif @translation =        'FTL Speed Mk I' then
                select 'Überlicht Geschwindigkeit Mk I' into @german;
            elseif @translation =        'A drive' then
                select 'Ein Antrieb' into @german;
            elseif @translation =        'A FTL drive' then
                select 'Ein Überlicht Antrieb' into @german;
            elseif @translation =        'Eternal live' then
                select 'Ewiges Leben' into @german;
            elseif @translation =        'Laboratories' then
                select 'Labore' into @german;
            elseif @translation =        'Laser' then
                select 'Laser' into @german;
            elseif @translation =        'Missile' then
                select 'Rakete' into @german;
            elseif @translation =        'Counter Missile' then
                select 'Gegenrakete' into @german;
            elseif @translation =        'Point Defense' then
                select 'Punktverteidigung' into @german;
            elseif @translation =        'Armor' then
                select 'Rüstung' into @german;
            elseif @translation =        'Shield' then
                select 'Schild' into @german;
            elseif @translation =        'Speed' then
                select 'Geschwindigkeit' into @german;
            elseif @translation =        'FTL Speed' then
                select 'Überlicht Geschwindigkeit' into @german;
            elseif @translation =        'Electronic Warfare' then
                select 'Elektronische Kriegsführung' into @german;
            elseif @translation =        'Rocket Ammunition' then
                select 'Raketenmunition' into @german;
            elseif @translation =        'Point Defense Ammunition' then
                select 'Punktverteidigungsmunition' into @german;
            elseif @translation =        'Counter Rocket Ammunition' then
                select 'Gegenraketenmunition' into @german;
            elseif @translation =        'Armor improvement I' then
                select 'Rüstungsverbesserung I' into @german;
            elseif @translation =        'Corvette' then
                select 'Korvette' into @german;
            elseif @translation =        'Frigate' then
                select 'Fregatte' into @german;
            elseif @translation =        'Cruiser' then
                select 'Kreuzer' into @german;
            elseif @translation =        'How to buy wine.' then
                select 'Wie man Wein kauft.' into @german;
            elseif @translation =        'The construction yard research researches the construction yard.' then
                select 'Die Bauhofforschung erforscht den Bauhof.' into @german;
            elseif @translation =        'The orbitals Construction Yard research researches the orbitals construction yard.' then
                select 'Die Orbital-Bauhofforschung erforscht den Orbital-Bauhof.' into @german;
            elseif @translation =        'The laboratories research researches laboratories.' then
                select 'Die Labore forschen forschen Labore.' into @german;
            elseif @translation =        'The Market place research researches Market places.' then
                select 'Die Marktplatzforschung erforscht Marktplätze.' into @german;
            elseif @translation =        'The Metal works research researches Metal works.' then
                select 'Die Metallwerksforschung erforscht Metallwerke.' into @german;
            elseif @translation =        'The Special orbital ores research researches Special orbital ores.' then
                select 'Die Spezialorbital-Erze-Forschung erforscht Spezialorbital-Erze.' into @german;
            elseif @translation =        'The Asynchronous Investigations research researches Asynchronous Investigations.' then
                select 'Die Forschung zu asynchronen Ermittlungen untersucht asynchrone Ermittlungen.' into @german;
            elseif @translation =        'The Laser research researches ...' then
                select 'Die Laserforschung erforscht ...' into @german;
            elseif @translation =        'The Missile research researches ...' then
                select 'Die Raketenforschung erforscht ...' into @german;
            elseif @translation =        'The Counter Missile research researches ...' then
                select 'Die Counter-Missile-Forschung erforscht ...' into @german;
            elseif @translation =        'The point defense research researches ...' then
                select 'Die Punktverteidigungsforschung forscht ...' into @german;
            elseif @translation =        'The Armor research researches ...' then
                select 'Die Rüstungsforschung erforscht ...' into @german;
            elseif @translation =        'The Shield research researches ...' then
                select 'Die Shield-Forschung erforscht ...' into @german;
            elseif @translation =        'The Speed research researches sub light ...' then
                select 'Die Speed-Forschung erforscht Unterlicht ...' into @german;
            elseif @translation =        'The FTL Speed research researches FTL ...' then
                select 'Die FTL-Speed-Forschung erforscht FTL ...' into @german;
            elseif @translation =        'The EW research researches electronic warfare.' then
                select 'Die EW-Forschung erforscht die elektronische Kriegsführung.' into @german;
            elseif @translation =        'a bunch of rockets.' then
                select 'ein Haufen Raketen.' into @german;
            elseif @translation =        'a bunch of bullets.' then
                select 'ein Haufen Kugeln.' into @german;
            elseif @translation =        'another bunch of rockets.' then
                select 'ein weiterer Haufen Raketen.' into @german;
            elseif @translation =        'Improves the armor improvement module' then
                select 'Verbessert das Rüstungsverbesserungsmodul' into @german;
            elseif @translation =        'The Corvette research researches Corvettes.' then
                select 'Die Korvettenforschung erforscht Korvetten.' into @german;
            elseif @translation =        'The Frigate research researches Frigates.' then
                select 'Die Fregattenforschung erforscht Fregatten.' into @german;
            elseif @translation =        'The Cruiser research researches Cruisers.' then
                select 'Die Kreuzerforschung erforscht Kreuzer.' into @german;
            elseif @translation =        'Shield Mk I' then
                select 'Schild Mk I' into @german;
            elseif @translation =        'A shield' then
                select 'Ein Schild' into @german;
            elseif @translation =        'Laser Mk I' then
                select 'Laser Mk I' into @german;
            elseif @translation =        'Point Defense Mk I' then
                select 'Punktverteidigung Mk I' into @german;
            elseif @translation =        'A laser' then
                select 'Ein Laser' into @german;
            elseif @translation =        'A point defense' then
                select 'Eine Punktverteidigung' into @german;
            elseif @translation =        'Nuclear ship killer war head' then
                select 'Atomschiff-Killer-Kriegskopf' into @german;
            elseif @translation =        'Counter war head' then
                select 'Gegenkriegskopf' into @german;
            elseif @translation =        'Ship Killer Motor Mk I' then
                select 'Schiff Killer Motor Mk I' into @german;
            elseif @translation =        'Counter Motor Mk I' then
                select 'Gegenmotor Mk I' into @german;
            elseif @translation =        'Nuclear ship killer missile Mk I' then
                select 'Nukleare Schiffskillerrakete Mk I' into @german;
            elseif @translation =        'Counter missile Mk I' then
                select 'Gegenrakete Mk I' into @german;
            else
                select concat('ERROR FOR: ', @translation, ' idTranslation: ' , @idTranslation) into @german;
            end if;
            #@formatter:on

            insert into translation (idTranslation, languageCode, translation) values (null, 'de', @german);
            select LAST_INSERT_ID() into @idTranslation;
            insert into translationCollection (idTranslatable, idTranslation) values (@idTranslatable, @idTranslation);
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertGermanTranslations();
drop procedure insertGermanTranslations;







