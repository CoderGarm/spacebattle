

    create table alignedAuraStates (
       idMovementAction integer not null,
        alignment varchar(255),
        antiMissileMissileRange varchar(255),
        antiShipMissileRange varchar(255),
        weaponRange varchar(255)
    ) engine=InnoDB;

    alter table alignedAuraStates
       add constraint FKqnx37coh1u0vvasr19plbsk5t
       foreign key (idMovementAction)
       references movementAction (idMovementAction);

INSERT INTO alignedAuraStates (idMovementAction, alignment, antiMissileMissileRange, antiShipMissileRange, weaponRange) SELECT idMovementAction, 'BOW', '0 M', '0 M', '0 M' from movementAction;
INSERT INTO alignedAuraStates (idMovementAction, alignment, antiMissileMissileRange, antiShipMissileRange, weaponRange) SELECT idMovementAction, 'BROADSIDE', '0 M', '0 M', '0 M' from movementAction;
INSERT INTO alignedAuraStates (idMovementAction, alignment, antiMissileMissileRange, antiShipMissileRange, weaponRange) SELECT idMovementAction, 'STERN', '0 M', '0 M', '0 M' from movementAction;

alter table movementAction drop column xCoordDestination;
alter table movementAction drop column yCoordDestination;


INSERT INTO dbPatch VALUES (NULL, NOW(), 'introduce aligned ranges', '0.1.18-1');
