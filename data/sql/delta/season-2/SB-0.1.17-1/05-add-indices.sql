create index IO_FL on fleet (isOperational);
create index ID_FL on fleet (isDeleted);
create index ID_FS on fleetSnapshot (isDeleted);
create index ID_JO on job (isDeleted);
create index ID_MI on mission (isDeleted);
create index TL_MO on move (ticksLeft);
create index ID_MO on move (isDeleted);
create index IO_OS on orbitalStructure (isOperational);
create index ID_OS on orbitalStructure (isDeleted);
create index ID_SC on shipClass (isDeleted);
create index TL_TR on tradedResource (ticksLeft);
create index ID_TR on tradedResource (isDeleted);
create index ID_TO on tradeOffer (isDeleted);
create index TL_TJ on transportJob (ticksLeft);
create index ID_TJ on transportJob (isDeleted);
create index IO_WS on warShip (isOperational);
create index ID_WS on warShip (isDeleted);
create index IO_WHSS on warshipHealthStateSnapshot (isOperational);
create index ID_WHSS on warshipHealthStateSnapshot (isDeleted);


INSERT INTO dbPatch VALUES (NULL, NOW(), 'add indices', '0.1.17-5');
