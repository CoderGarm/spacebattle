
    create table heatMap (
                             idHeatMap   integer      not null auto_increment,
                             heat        integer      not null,
                             missionType varchar(255) not null,
                             idPlanet    integer      not null,
                             primary key (idHeatMap)
    ) engine = InnoDB;

    alter table heatMap
        add constraint FKafi78glkfnkhp6l63bjifnuqr
            foreign key (idPlanet)
                references planet (idPlanet);

    INSERT INTO heatMap (heat, missionType, idPlanet)
    SELECT 1, 'PIRATE_RAID', idPlanet
    FROM planet p
             LEFT JOIN user u on (p.idOwner = u.idUser)
    WHERE u.dType = 'USER';

    DELIMITER |
    CREATE PROCEDURE setupHeatMap()
    BEGIN
        DECLARE n INT DEFAULT 0;
        DECLARE i INT DEFAULT 0;
        SELECT COUNT(*) FROM heatMap INTO n;
        SET i = 0;
        WHILE i < n
            DO
            select h.idHeatMap, h.idPlanet from heatMap h order by h.idHeatMap offset i rows fetch next 1 row only into @idHeatMap, @idPlanet;

            select idOwner from planet where idPlanet = @idPlanet into @idOwner;

            select count(idPlanet) from planet where idOwner = @idOwner into @planetCount;

            select count(f.idFleet) from fleet f where f.isDeleted = false and f.isOperational = true and f.idOwner = @idOwner into @fleetCount;

            select least(@planetCount, @fleetCount)*2 into @min;

            select greatest(@planetCount, @fleetCount)*2 into @max;

            select floor(@min+rand()*(@max-@min)) into @effectiveHeat;

            update heatMap h set heat = @effectiveHeat where idHeatMap = @idHeatMap;

            SET i = i + 1;
            END WHILE;
    End |
    DELIMITER ;

    call setupHeatMap();
    drop procedure setupHeatMap;

#--select p.idPlanet, p.name, username, heat from heatMap left join planet p on heatMap.idPlanet = p.idPlanet left join user u on p.idOwner = u.idUser order by heat desc;

    insert into dbPatch
    values (null, now(), 'heat map', '0.1.6-1');
