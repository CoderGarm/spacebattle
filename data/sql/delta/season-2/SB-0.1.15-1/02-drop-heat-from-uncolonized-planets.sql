delete from heatMap where idPlanet in (select p.idPlanet from planet p where idOwner is null);

INSERT INTO dbPatch VALUES (NULL, NOW(), 'drop heat from uncolonized planets', '0.1.15-2');
