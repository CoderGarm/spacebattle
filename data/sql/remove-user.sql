

delete from researchLevels where idUser = 12;
delete from fleet where idOwner = 12;
delete from userMessage where idMessageThread in (select idMessageThread from messageThread where idUserOne = 12 or idUserTwo = 12);
delete from messageThread where idUserOne = 12 or idUserTwo = 12;
update planet set isMain = false, idOwner = null where idOwner = 12;


delete from alignedFitting where idShipClass in (select idShipClass from shipClass where idOwner = 12);
delete from ammunitionFitting where idShipClass in (select idShipClass from shipClass where idOwner = 12);
delete from supportFitting where idShipClass in (select idShipClass from shipClass where idOwner = 12);

delete from shipClass where idOwner = 12;
delete from knownStarSystem where idOwner = 12;


delete from user where idUser = 12;
