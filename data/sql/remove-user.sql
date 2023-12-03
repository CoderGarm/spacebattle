

delete from researchLevels where idUser = 25;
delete from fleet where idOwner = 25;
delete from job where idOwner = 25;
delete from userMessage where idMessageThread in (select idMessageThread from messageThread where idUserOne = 25 or idUserTwo = 25);
delete from messageThread where idUserOne = 25 or idUserTwo = 25;
delete from construction where idPlanet = (select idPlanet from planet where idOwner = 25);
delete from orbitalStructure where idPlanet = (select idPlanet from planet where idOwner = 25);
update planet set isMain = false, idOwner = null where idOwner = 25;

delete from alignedFitting where idShipClass in (select idShipClass from shipClass where idOwner = 25);
delete from ammunitionFitting where idShipClass in (select idShipClass from shipClass where idOwner = 25);
delete from supportFitting where idShipClass in (select idShipClass from shipClass where idOwner = 25);

delete from shipClass where idOwner = 25;
delete from knownStarSystem where idOwner = 25;


delete from userSetting where idUser = 25;
delete from user where idUser = 25;
