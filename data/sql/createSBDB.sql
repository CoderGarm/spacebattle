
    create table activeFittings (
       idWarshipHealthState integer not null,
        amount integer not null,
        idLauncher integer,
        idWeapon integer,
        weaponAlignment varchar(255)
    ) engine=InnoDB;

    create table activeFittingsSnapshot (
       idWarshipHealthStateSnapshot integer not null,
        amount integer not null,
        idLauncher integer,
        idWeapon integer,
        weaponAlignment varchar(255)
    ) engine=InnoDB;

    create table alignedAuraStates (
       idMovementAction integer not null,
        alignment varchar(255),
        antiMissileMissileRange varchar(255),
        antiShipMissileRange varchar(255),
        weaponRange varchar(255)
    ) engine=InnoDB;

    create table alignedFitting (
       idShipClass integer not null,
        amount integer not null,
        idLauncher integer,
        idWeapon integer,
        weaponAlignment varchar(255)
    ) engine=InnoDB;

    create table alliance (
       idAlliance integer not null auto_increment,
        code varchar(30) not null,
        createdAt datetime(6) not null,
        leftBottom longtext,
        leftUpper longtext,
        rightBottom longtext,
        rightUpper longtext,
        name varchar(30) not null,
        idFounder integer not null,
        primary key (idAlliance)
    ) engine=InnoDB;

    create table allianceApplication (
       idAllianceApplication integer not null auto_increment,
        applicationState varchar(255) not null,
        idAlliance integer not null,
        idUser integer not null,
        idTickAppliedAt integer not null,
        idTickDecidedAt integer,
        primary key (idAllianceApplication)
    ) engine=InnoDB;

    create table allowedMissiles (
       idLauncher integer not null,
        idMissile integer not null,
        primary key (idLauncher, idMissile)
    ) engine=InnoDB;

    create table ammunitionFitting (
       idShipClass integer not null,
        amount integer not null,
        idMissile integer
    ) engine=InnoDB;

    create table armor (
       idArmor integer not null auto_increment,
        technicalTypeName varchar(255) not null,
        unlockedThroughLevel integer not null,
        effectValue integer not null,
        shipClassType varchar(255) not null,
        tonnage varchar(255) not null,
        idNamedTechLevel integer not null,
        idCosts integer not null,
        primary key (idArmor)
    ) engine=InnoDB;

    create table article (
       idArticle integer not null auto_increment,
        langCode varchar(255) not null,
        title varchar(255) not null,
        tutorialCategory varchar(255),
        wikiCategory varchar(255) not null,
        idBase integer,
        primary key (idArticle)
    ) engine=InnoDB;

    create table article_articleRevisions (
       Article_idArticle integer not null,
        articleRevisions_idArticleRevision integer not null,
        primary key (Article_idArticle, articleRevisions_idArticleRevision)
    ) engine=InnoDB;

    create table articleLines (
       idArticleRevision integer not null,
        content varchar(255),
        deltaType varchar(255) not null,
        lineNo integer not null
    ) engine=InnoDB;

    create table articleRevision (
       idArticleRevision integer not null auto_increment,
        version integer not null,
        idArticle integer not null,
        idAuthor integer not null,
        primary key (idArticleRevision)
    ) engine=InnoDB;

    create table battleReport (
       idBattleReport integer not null auto_increment,
        lastRound integer not null,
        uuid varchar(255) not null,
        xCoordinate varchar(255),
        yCoordinate varchar(255),
        idTick integer not null,
        idPlanet integer,
        idStarSystem integer,
        primary key (idBattleReport)
    ) engine=InnoDB;

    create table building (
       idBuilding integer not null auto_increment,
        techLevel varchar(255) not null,
        baseValue integer not null,
        increasingFactorPerLevel decimal(19,2),
        productionCategory varchar(255) not null,
        productionTarget varchar(255) not null,
        refinementSequence varchar(255),
        unlockedThroughLevel integer not null,
        idTranslatableDescription integer not null,
        idTranslatableName integer not null,
        idCosts integer not null,
        idResearch integer not null,
        primary key (idBuilding)
    ) engine=InnoDB;

    create table colonization (
       idColonization integer not null auto_increment,
        doneAtZero integer not null,
        isPlanned bit not null,
        idCosts integer not null,
        idTarget integer not null,
        idUser integer not null,
        primary key (idColonization)
    ) engine=InnoDB;

    create table construction (
       idConstruction integer not null auto_increment,
        isOperational boolean not null default false,
        level integer not null,
        operationalLevel integer not null,
        idTickActivated integer,
        idBuilding integer not null,
        idPlanet integer not null,
        primary key (idConstruction)
    ) engine=InnoDB;

    create table counterMissileHit (
       idCounterMissileHit integer not null auto_increment,
        combatPhase varchar(255) not null,
        combatRound integer not null,
        attackedMissileSalvo varchar(255) not null,
        destroyedMissiles integer not null,
        remainingMissiles integer not null,
        idActor integer not null,
        idMissile integer not null,
        idTarget integer not null,
        primary key (idCounterMissileHit)
    ) engine=InnoDB;

    create table counterMissileHits (
       idBattleReport integer not null,
        idCounterMissileHit integer not null,
        primary key (idBattleReport, idCounterMissileHit)
    ) engine=InnoDB;

    create table dbPatch (
       idDBPatch integer not null auto_increment,
        createdAt datetime(6) not null,
        description varchar(255) not null,
        version varchar(255) not null,
        primary key (idDBPatch)
    ) engine=InnoDB;

    create table electronicWarfare (
       idElectronicWarfare integer not null auto_increment,
        technicalTypeName varchar(255) not null,
        unlockedThroughLevel integer not null,
        effectValue integer not null,
        shipClassType varchar(255) not null,
        tonnage varchar(255) not null,
        effectiveRange varchar(255),
        idNamedTechLevel integer not null,
        idCosts integer not null,
        primary key (idElectronicWarfare)
    ) engine=InnoDB;

    create table eventRanking (
       idEventRanking integer not null auto_increment,
        gameEvent varchar(255) not null,
        points integer not null,
        rankingCategory varchar(255) not null,
        idUser integer not null,
        primary key (idEventRanking)
    ) engine=InnoDB;

    create table fleet (
       idFleet integer not null auto_increment,
        isDeleted boolean not null default false,
        isOperational boolean not null default false,
        name varchar(255) not null,
        xCoordinateLocation varchar(255),
        yCoordinateLocation varchar(255),
        idTickActivated integer,
        idMove integer,
        idPlanetLocation integer,
        idStarSystemLocation integer,
        idOwner integer not null,
        idResourceDeposit integer,
        primary key (idFleet)
    ) engine=InnoDB;

    create table fleetSnapshot (
       idFleetSnapshot integer not null auto_increment,
        isDeleted boolean not null default false,
        name varchar(255) not null,
        idBattleReport integer,
        idFleet integer not null,
        idOwner integer not null,
        primary key (idFleetSnapshot)
    ) engine=InnoDB;

    create table flightPlan (
       dType varchar(31) not null,
        idFlightPlan integer not null auto_increment,
        xCoordinate varchar(255),
        yCoordinate varchar(255),
        timeAfterStart decimal(3, 0),
        idPlanet integer,
        idStarSystem integer,
        idMove integer,
        primary key (idFlightPlan)
    ) engine=InnoDB;

    create table forum (
       idForum integer not null auto_increment,
        createdAt datetime(6) not null,
        description varchar(255) not null,
        role varchar(255),
        title varchar(255) not null,
        idAlliance integer,
        primary key (idForum),
        constraint forum_CHECK check (idAlliance IS NOT NULL OR role IS NOT NULL)
    ) engine=InnoDB;

    create table forumMessage (
       idForumMessage integer not null auto_increment,
        message varchar(10000) not null,
        sentAt datetime(6) not null,
        idUserAuthor integer not null,
        idForumThread integer not null,
        primary key (idForumMessage)
    ) engine=InnoDB;

    create table forumMessageRead (
       idForumMessageRead integer not null auto_increment,
        isRead boolean not null default false,
        idForum integer not null,
        idForumMessage integer not null,
        idForumThread integer not null,
        idUser integer not null,
        primary key (idForumMessageRead)
    ) engine=InnoDB;

    create table forumThread (
       idForumThread integer not null auto_increment,
        createdAt datetime(6) not null,
        description varchar(255) not null,
        lastChanged datetime(6) not null,
        title varchar(255) not null,
        idForum integer not null,
        primary key (idForumThread)
    ) engine=InnoDB;

    create table heatMap (
       idHeatMap integer not null auto_increment,
        heat integer not null,
        missionType varchar(255) not null,
        idPlanet integer not null,
        primary key (idHeatMap)
    ) engine=InnoDB;

    create table hitLog (
       idHitLog integer not null auto_increment,
        combatPhase varchar(255) not null,
        combatRound integer not null,
        attackedPart varchar(255) not null,
        damageDealer varchar(255) not null,
        damageValue bigint not null,
        isAlive boolean not null default true,
        isFightingCapable boolean not null default true,
        state integer not null,
        idTarget integer not null,
        primary key (idHitLog)
    ) engine=InnoDB;

    create table humanResources (
       idResourceDeposit integer not null,
        amount decimal(19, 0) not null,
        educationType varchar(50) not null,
        primary key (idResourceDeposit, educationType)
    ) engine=InnoDB;

    create table job (
       idJob integer not null auto_increment,
        isDeleted boolean not null default false,
        pointsLeft decimal(19, 0) not null,
        jobType varchar(255),
        resourceType varchar(255),
        targetLevel integer,
        priority varchar(255) not null,
        idTickCompleted integer,
        idBuilding integer,
        idFleet integer,
        idFleetSnapshot integer,
        idResearch integer,
        idFacility integer not null,
        idOwner integer not null,
        primary key (idJob)
    ) engine=InnoDB;

    create table knownStarSystem (
       idOwner integer not null,
        idStarSystem integer not null,
        primary key (idOwner, idStarSystem)
    ) engine=InnoDB;

    create table launcher (
       idLauncher integer not null auto_increment,
        technicalTypeName varchar(255) not null,
        unlockedThroughLevel integer not null,
        effectValue integer not null,
        shipClassType varchar(255) not null,
        tonnage varchar(255) not null,
        weaponType varchar(255) not null,
        idNamedTechLevel integer not null,
        idCosts integer not null,
        primary key (idLauncher),
        constraint launcher_CHECK check (weaponType = 'MISSILE' OR weaponType = 'COUNTER_MISSILE')
    ) engine=InnoDB;

    create table lossesByHit (
       idShipKillerHit integer not null,
        idFleet integer,
        idWarship integer not null,
        idOwner integer,
        idShipClass integer,
        warShipName varchar(255),
        idHitLog integer not null,
        primary key (idShipKillerHit, idHitLog)
    ) engine=InnoDB;

    create table messageThread (
       idMessageThread integer not null auto_increment,
        idUserOne integer not null,
        idUserTwo integer not null,
        primary key (idMessageThread)
    ) engine=InnoDB;

    create table miningFactors (
       idMiningFactors integer not null auto_increment,
        primary key (idMiningFactors)
    ) engine=InnoDB;

    create table miningFactorsComposition (
       idMiningFactors integer not null,
        amount decimal(19, 0),
        resourceType varchar(50) not null,
        primary key (idMiningFactors, resourceType)
    ) engine=InnoDB;

    create table missile (
       idMissile integer not null auto_increment,
        technicalTypeName varchar(255) not null,
        unlockedThroughLevel integer not null,
        elokaResistance integer,
        shipClassType varchar(255) not null,
        tonnage varchar(255) not null,
        acceleration varchar(255),
        endurance integer not null,
        maneuverability integer not null,
        damageProjectionRange varchar(255),
        damageValue bigint not null,
        warheadType varchar(255),
        idNamedTechLevel integer not null,
        idCosts integer not null,
        primary key (idMissile)
    ) engine=InnoDB;

    create table missileMovement (
       idMissileMovement integer not null auto_increment,
        combatPhase varchar(255) not null,
        combatRound integer not null,
        xCoordLast varchar(255),
        yCoordLast varchar(255),
        missileAmount integer not null,
        movingMissileSalvo varchar(255) not null,
        xCoordinate varchar(255),
        yCoordinate varchar(255),
        roundsToTravel integer not null,
        xCoordTarget varchar(255),
        yCoordTarget varchar(255),
        idActor integer not null,
        idTarget integer not null,
        primary key (idMissileMovement)
    ) engine=InnoDB;

    create table missileMovements (
       idBattleReport integer not null,
        idMissileMovement integer not null,
        primary key (idBattleReport, idMissileMovement)
    ) engine=InnoDB;

    create table mission (
       missionType varchar(31) not null,
        idMission integer not null auto_increment,
        isDeleted boolean not null default false,
        idActor integer not null,
        idTickStartedAt integer not null,
        idTickStoppedAt integer,
        idTradeResource integer,
        idPlanet integer,
        primary key (idMission)
    ) engine=InnoDB;

    create table missionItem (
       missionType varchar(31) not null,
        idMissionItem integer not null auto_increment,
        isRansomPayment bit,
        percentOfCargoLost integer,
        phase varchar(255),
        piratedWithdraw bit,
        piratedWithdrawAfterApproach bit,
        idTickCreatedAt integer not null,
        idTradeResource integer,
        primary key (idMissionItem)
    ) engine=InnoDB;

    create table move (
       idMove integer not null auto_increment,
        isDeleted boolean not null default false,
        ticksLeft decimal(19, 0) not null,
        xCoordinateDestination varchar(255),
        yCoordinateDestination varchar(255),
        xCoordinateOrigin varchar(255),
        yCoordinateOrigin varchar(255),
        originalDuration integer,
        idTickCompleted integer,
        idPlanetDestination integer,
        idStarSystemDestination integer,
        idFleet integer,
        idFleetSnapshot integer,
        idPlanetOrigin integer,
        idStarSystemOrigin integer,
        idUser integer not null,
        idTickStarted integer not null,
        primary key (idMove),
        constraint move_CHECK check (xCoordinateOrigin != xCoordinateDestination AND yCoordinateOrigin != yCoordinateDestination)
    ) engine=InnoDB;

    create table movementAction (
       idMovementAction integer not null auto_increment,
        combatPhase varchar(255) not null,
        combatRound integer not null,
        xCoordInterimDestination varchar(255),
        yCoordInterimDestination varchar(255),
        movementType varchar(255) not null,
        xCoordinate varchar(255),
        yCoordinate varchar(255),
        idActor integer not null,
        primary key (idMovementAction)
    ) engine=InnoDB;

    create table movementActions (
       idBattleReport integer not null,
        idMovementAction integer not null,
        primary key (idBattleReport, idMovementAction)
    ) engine=InnoDB;

    create table namedTechLevel (
       idNamedTechLevel integer not null auto_increment,
        techLevel varchar(255) not null,
        translationTarget varchar(255) not null,
        idTranslatableDescription integer not null,
        idTranslatableName integer not null,
        idResearch integer not null,
        primary key (idNamedTechLevel)
    ) engine=InnoDB;

    create table orbitalModule (
       idOrbitalModule integer not null auto_increment,
        techLevel varchar(255) not null,
        baseValue integer not null,
        effect varchar(255) not null,
        unlockedThroughLevel integer not null,
        idTranslatableDescription integer not null,
        idTranslatableName integer not null,
        idCosts integer not null,
        idResearch integer not null,
        primary key (idOrbitalModule)
    ) engine=InnoDB;

    create table orbitalModuleJobElements (
       idJob integer not null,
        amount integer not null,
        idOrbitalModule integer
    ) engine=InnoDB;

    create table orbitalStructure (
       idOrbitalStructure integer not null auto_increment,
        isDeleted boolean not null default false,
        isOperational boolean not null default false,
        amount integer not null,
        xCoordinate varchar(255),
        yCoordinate varchar(255),
        idTickActivated integer,
        idOrbitalModule integer not null,
        idPlanet integer,
        idStarSystem integer,
        idOwner integer not null,
        primary key (idOrbitalStructure)
    ) engine=InnoDB;

    create table orderedHitLog (
       idShipKillerHit integer not null,
        idHitLog integer not null,
        orderNo integer not null,
        primary key (idShipKillerHit, orderNo)
    ) engine=InnoDB;

    create table participatingFleets (
       idBattleReport integer not null,
        idFleetSnapshot integer not null,
        primary key (idBattleReport, idFleetSnapshot)
    ) engine=InnoDB;

    create table participatingUsers (
       idSharedBattleReport integer not null,
        idUser integer not null,
        primary key (idSharedBattleReport, idUser)
    ) engine=InnoDB;

    create table passiveModule (
       idPassiveModule integer not null auto_increment,
        techLevel varchar(255) not null,
        calculationType varchar(255) not null,
        effectValue integer not null,
        shipClassType varchar(255) not null,
        supportType varchar(255) not null,
        tonnage varchar(255) not null,
        unlockedThroughLevel integer not null,
        idTranslatableDescription integer not null,
        idTranslatableName integer not null,
        idCosts integer not null,
        idResearch integer not null,
        primary key (idPassiveModule)
    ) engine=InnoDB;

    create table planet (
       idPlanet integer not null auto_increment,
        colonizedAt datetime(6),
        isMain boolean not null default false,
        name varchar(30) not null,
        xCoordinate varchar(255),
        yCoordinate varchar(255),
        idMiningFactors integer not null,
        idOwner integer,
        idResourceDeposit integer,
        idResourceTransportationDelivery integer,
        idResourceTransportationDemand integer,
        idStarSystem integer,
        primary key (idPlanet)
    ) engine=InnoDB;

    create table propulsion (
       idPropulsion integer not null auto_increment,
        technicalTypeName varchar(255) not null,
        unlockedThroughLevel integer not null,
        costsPercentage integer not null,
        effectValue integer not null,
        hyperBand varchar(255) not null,
        technologyType varchar(255) not null,
        idNamedTechLevel integer not null,
        primary key (idPropulsion)
    ) engine=InnoDB;

    create table releasedVolley (
       idReleasedVolley integer not null auto_increment,
        combatPhase varchar(255) not null,
        combatRound integer not null,
        amountOfShots integer not null,
        damageDealer varchar(255) not null,
        initialDistance varchar(255) not null,
        weaponType varchar(255) not null,
        idActor integer not null,
        idTarget integer not null,
        primary key (idReleasedVolley)
    ) engine=InnoDB;

    create table releasesVolleys (
       idBattleReport integer not null,
        idReleasedVolley integer not null,
        primary key (idBattleReport, idReleasedVolley)
    ) engine=InnoDB;

    create table remainingShots (
       idWarshipHealthState integer not null,
        amount decimal(19, 0) not null,
        idMissile integer not null,
        primary key (idWarshipHealthState, idMissile)
    ) engine=InnoDB;

    create table remainingShotsSnapshot (
       idWarshipHealthStateSnapshot integer not null,
        amount decimal(19, 0) not null,
        idMissile integer not null,
        primary key (idWarshipHealthStateSnapshot, idMissile)
    ) engine=InnoDB;

    create table research (
       idResearch integer not null auto_increment,
        techLevel varchar(255) not null,
        levelCap integer not null,
        idTranslatableDescription integer not null,
        idTranslatableName integer not null,
        idCosts integer not null,
        unlockedThrough integer,
        primary key (idResearch)
    ) engine=InnoDB;

    create table researchLevels (
       idResearchLevel integer not null auto_increment,
        level integer not null,
        idResearch integer not null,
        idUser integer not null,
        primary key (idResearchLevel)
    ) engine=InnoDB;

    create table resourceDeposit (
       idResourceDeposit integer not null auto_increment,
        subType varchar(255) not null,
        primary key (idResourceDeposit)
    ) engine=InnoDB;

    create table resourcesDepositComposition (
       idResourceDeposit integer not null,
        amount decimal(19, 0) not null,
        resourceType varchar(50) not null,
        primary key (idResourceDeposit, resourceType)
    ) engine=InnoDB;

    create table rolePlaySetting (
       idRolePlaySetting integer not null auto_increment,
        leftBottom longtext,
        leftUpper longtext,
        rightBottom longtext,
        rightUpper longtext,
        empireName varchar(50),
        firstname varchar(50),
        participant varchar(255) not null,
        shipNameTemplates varchar(255) not null,
        shipNames longtext,
        shipPrefix varchar(6),
        surname varchar(50),
        title varchar(50),
        titleAbbreviation varchar(8),
        winner varchar(255) not null,
        idUser integer not null,
        primary key (idRolePlaySetting)
    ) engine=InnoDB;

    create table sharedBattleReport (
       idSharedBattleReport integer not null auto_increment,
        shareWithEveryone boolean not null default false,
        idBattleReport integer not null,
        primary key (idSharedBattleReport)
    ) engine=InnoDB;

    create table sharedWithAlliances (
       idSharedBattleReport integer not null,
        idAlliance integer not null,
        primary key (idSharedBattleReport, idAlliance)
    ) engine=InnoDB;

    create table sharedWithUsers (
       idSharedBattleReport integer not null,
        idUser integer not null,
        primary key (idSharedBattleReport, idUser)
    ) engine=InnoDB;

    create table shipClass (
       idShipClass integer not null auto_increment,
        isDeleted boolean not null default false,
        name varchar(30) not null,
        shipClassType varchar(255) not null,
        idArmor integer,
        idElectronicWarfare integer,
        idOwner integer not null,
        idPredecessor integer,
        idPropulsion integer not null,
        idSidewall integer,
        idSuccessor integer,
        primary key (idShipClass)
    ) engine=InnoDB;

    create table shipKillerHit (
       idShipKillerHit integer not null auto_increment,
        combatPhase varchar(255) not null,
        combatRound integer not null,
        damageDealer varchar(255) not null,
        distance varchar(255) not null,
        result varchar(255) not null,
        idActor integer not null,
        idTarget integer not null,
        primary key (idShipKillerHit)
    ) engine=InnoDB;

    create table shipKillerHits (
       idBattleReport integer not null,
        idShipKillerHit integer not null,
        primary key (idBattleReport, idShipKillerHit)
    ) engine=InnoDB;

    create table sidewall (
       idSidewall integer not null auto_increment,
        technicalTypeName varchar(255) not null,
        unlockedThroughLevel integer not null,
        effectValue integer not null,
        shipClassType varchar(255) not null,
        tonnage varchar(255) not null,
        idNamedTechLevel integer not null,
        idCosts integer not null,
        primary key (idSidewall)
    ) engine=InnoDB;

    create table starSystem (
       idStarSystem integer not null auto_increment,
        name varchar(255) not null,
        xCoordinate varchar(255),
        yCoordinate varchar(255),
        primary key (idStarSystem)
    ) engine=InnoDB;

    create table supportFitting (
       idShipClass integer not null,
        amount integer not null,
        idPassiveModule integer
    ) engine=InnoDB;

    create table tick (
       idTick integer not null auto_increment,
        tickEnds datetime(6),
        tickStarts datetime(6) not null,
        primary key (idTick)
    ) engine=InnoDB;

    create table tradedResource (
       idTradedResource integer not null auto_increment,
        isDeleted boolean not null default false,
        ticksLeft decimal(19, 0) not null,
        idTickCompleted integer,
        idBuyer integer not null,
        idDestination integer not null,
        idTickInitiated integer not null,
        idTradeOffer integer,
        primary key (idTradedResource)
    ) engine=InnoDB;

    create table tradeOffer (
       idTradeOffer integer not null auto_increment,
        isDeleted boolean not null default false,
        amount bigint not null,
        resourceType varchar(255) not null,
        unitPrice bigint not null,
        idOrigin integer,
        idSeller integer,
        idTickInitiated integer not null,
        primary key (idTradeOffer)
    ) engine=InnoDB;

    create table transferredShips (
       idTransportJob integer not null,
        idWarship integer not null,
        primary key (idTransportJob, idWarship)
    ) engine=InnoDB;

    create table translatable (
       idTranslatable integer not null auto_increment,
        idParent integer not null,
        translatableType varchar(255) not null,
        translationTarget varchar(255) not null,
        primary key (idTranslatable)
    ) engine=InnoDB;

    create table translation (
       idTranslation integer not null auto_increment,
        languageCode varchar(3),
        translation varchar(400),
        idTranslatable integer,
        primary key (idTranslation)
    ) engine=InnoDB;

    create table transportJob (
       idTransportJob integer not null auto_increment,
        isDeleted boolean not null default false,
        ticksLeft decimal(19, 0) not null,
        idTickCompleted integer,
        idDestination integer not null,
        idOrigin integer not null,
        idOwner integer not null,
        idTickInitiated integer not null,
        primary key (idTransportJob)
    ) engine=InnoDB;

    create table user (
       dType varchar(31) not null,
       idUser integer not null auto_increment,
        username varchar(30) not null,
        gameUserRoles varchar(255),
        userRole varchar(255),
        idAlliance integer,
        primary key (idUser)
    ) engine=InnoDB;

    create table userMessage (
       idUserMessage integer not null auto_increment,
        message varchar(10000) not null,
        receivedAt datetime(6),
        sentAt datetime(6) not null,
        idMessageThread integer not null,
        idUserSender integer not null,
        primary key (idUserMessage)
    ) engine=InnoDB;

    create table userSetting (
       idUserSetting integer not null auto_increment,
        createdAt datetime(6) not null,
        email varchar(50) not null,
        isEMailVerified boolean not null default false,
        isLoginForbidden boolean not null default false,
        noEMailWanted boolean not null default false,
        password varchar(255) not null,
        profilePic varchar(50) default 'perspective-dice-six-faces-random' not null,
        receiveChangelogInfos boolean not null default false,
        receiveTickAdvice boolean not null default false,
        idUser integer not null,
        primary key (idUserSetting)
    ) engine=InnoDB;

    create table warShip (
       idWarShip integer not null auto_increment,
        isDeleted boolean not null default false,
        isOperational boolean not null default false,
        name varchar(255) not null,
        idTickActivated integer,
        idFleet integer,
        idMission integer,
        idMothball integer,
        idTransportJob integer,
        idShipClass integer not null,
        idShipyard integer not null,
        primary key (idWarShip)
    ) engine=InnoDB;

    create table warshipCapabilities (
       idWarshipHealthState integer not null,
        moduleType varchar(255),
        value decimal(19, 0)
    ) engine=InnoDB;

    create table warshipCapabilitiesSnapshot (
       idWarshipHealthStateSnapshot integer not null,
        moduleType varchar(255),
        value decimal(19, 0)
    ) engine=InnoDB;

    create table warshipHealthState (
       idWarshipHealthState integer not null auto_increment,
        isFightingCapable boolean not null default true,
        idWarship integer not null,
        primary key (idWarshipHealthState)
    ) engine=InnoDB;

    create table warshipHealthStateSnapshot (
       idWarshipHealthStateSnapshot integer not null auto_increment,
        isDeleted boolean not null default false,
        isOperational boolean not null default false,
        isFightingCapable boolean not null default true,
        idTickActivated integer,
        idFleetSnapshot integer not null,
        idWarship integer not null,
        primary key (idWarshipHealthStateSnapshot)
    ) engine=InnoDB;

    create table weapon (
       idWeapon integer not null auto_increment,
        technicalTypeName varchar(255) not null,
        unlockedThroughLevel integer not null,
        effectValue integer not null,
        shipClassType varchar(255) not null,
        tonnage varchar(255) not null,
        amountDamageEmitter integer not null,
        damageProjectionRange varchar(255),
        weaponType varchar(255) not null,
        idNamedTechLevel integer not null,
        idCosts integer not null,
        primary key (idWeapon),
        constraint weapon_CHECK check (weaponType = 'BEAM' OR weaponType = 'POINT_DEFENSE')
    ) engine=InnoDB;

    alter table alliance
       add constraint UK_h7jfng3csi7xy8d1r3dqe07lo unique (code);

    alter table alliance
        add constraint UK_7nuq4ufi5qsmpn1u6i8n2nxot unique (name);

    alter table article_articleRevisions
        add constraint UK_oq8mr3fxh6t655kgme0wxg7pa unique (articleRevisions_idArticleRevision);

    alter table construction
        add constraint CONSTRUCTION_UK unique (idPlanet, idBuilding);

    alter table counterMissileHits
        add constraint UK_5t1h6fs2csdnofihl1lsysbf9 unique (idCounterMissileHit);

    alter table eventRanking
       add constraint POINTS_UK unique (idUser, gameEvent, rankingCategory);

    alter table fleet
        add constraint UK_duhimx7ydhmssl7vqp5w29yx0 unique (idMove);

    alter table messageThread
        add constraint messageThread_UC unique (idUserOne, idUserTwo);

    alter table missileMovements
        add constraint UK_7i21tt2alyj9dggioukympy2t unique (idMissileMovement);

    alter table movementActions
        add constraint UK_k7lndwnt1rxmmjj2xslfrkuv unique (idMovementAction);

    alter table orderedHitLog
        add constraint UK_mpoyl8losmb1ep0fxewrpbuf3 unique (idHitLog);

    alter table planet
        add constraint PLANET_UK unique (idStarSystem, idPlanet, xCoordinate, yCoordinate);

    alter table releasesVolleys
        add constraint UK_hsr966dv9qpnj1i7nhg3nlbc6 unique (idReleasedVolley);

    alter table rolePlaySetting
       add constraint UK_5sx33g2kpg6lhpamw75ibqb9i unique (idUser);

    alter table sharedBattleReport
       add constraint UK_hpe1gr8wmca659sm228uhgp6u unique (idBattleReport);
create index ID_SC on shipClass (isDeleted);

    alter table shipClass
        add constraint UK_4sgs4ew920mkttyjueq19n70q unique (idPredecessor);

    alter table shipClass
        add constraint UK_kqyh4et3r89d2iy3w2sggpt90 unique (idSuccessor);

    alter table shipKillerHits
        add constraint UK_nd3dfq3yjyhaauawah5lm5mj2 unique (idShipKillerHit);

    alter table starSystem
        add constraint COORDINATE_UK unique (xCoordinate, yCoordinate);

    alter table user
        add constraint UK_sb8bbouer5wak8vyiiy4pf2bx unique (username);

    alter table userSetting
       add constraint UK_de78bv8lgkrdwqxfpqr8k3wfu unique (idUser);

    alter table userSetting
       add constraint EMAIL_UK unique (email);

    alter table activeFittings
        add constraint FK9kawhm3fqubxvebrl8pjl10lv
            foreign key (idLauncher)
                references launcher (idLauncher);

    alter table activeFittings
        add constraint FKme68sre9mt8ikil6n15ax2grn
            foreign key (idWeapon)
                references weapon (idWeapon);

    alter table activeFittings
        add constraint FKqf0cnnigkfynfaf6jok2lby74
            foreign key (idWarshipHealthState)
                references warshipHealthState (idWarshipHealthState);

    alter table activeFittingsSnapshot
        add constraint FKlw1s63u5x690vuag5ym70upqw
            foreign key (idLauncher)
                references launcher (idLauncher);

    alter table activeFittingsSnapshot
        add constraint FK8ug5h1r2xixwuogyl5n6y2jy2
            foreign key (idWeapon)
                references weapon (idWeapon);

    alter table activeFittingsSnapshot
        add constraint FKm7x7patbu0qiko7r2v7hdnoi
            foreign key (idWarshipHealthStateSnapshot)
                references warshipHealthStateSnapshot (idWarshipHealthStateSnapshot);

    alter table alignedAuraStates
       add constraint FKqnx37coh1u0vvasr19plbsk5t
       foreign key (idMovementAction)
       references movementAction (idMovementAction);

    alter table alignedFitting
        add constraint FKkhnl9hmtdgol96bsu6d5csqxg
            foreign key (idLauncher)
                references launcher (idLauncher);

    alter table alignedFitting
        add constraint FKt6aos80sh8332mepbkuwmo98i
            foreign key (idWeapon)
                references weapon (idWeapon);

    alter table alignedFitting
        add constraint FKgdp5e1ylgswr29e2d5b7uhib
            foreign key (idShipClass)
                references shipClass (idShipClass);

    alter table alliance
        add constraint FKqtn90ky0waqf7lslqa7gu66mo
            foreign key (idFounder)
                references user (idUser);

    alter table allianceApplication
        add constraint FK1awqwgyqyd6ij150b4dwhoa8t
            foreign key (idAlliance)
                references alliance (idAlliance);

    alter table allianceApplication
        add constraint FKibkneamgqu1yjkxmtn09gn7ct
            foreign key (idUser)
                references user (idUser);

    alter table allianceApplication
        add constraint FK90anukwo3tqyrbwum7veaglgp
            foreign key (idTickAppliedAt)
                references tick (idTick);

    alter table allianceApplication
        add constraint FKn3vxrm2s3ytx9ivggr9fflnqh
            foreign key (idTickDecidedAt)
                references tick (idTick);

    alter table allowedMissiles
        add constraint FKhp9tc55hay9lojn6swpo6q4kv
            foreign key (idMissile)
                references missile (idMissile);

    alter table allowedMissiles
        add constraint FK1dns3uxovqh388wtp38xk3l8p
            foreign key (idLauncher)
                references launcher (idLauncher);

    alter table ammunitionFitting
        add constraint FK3bacpw2vspgqtq2trokhge1g
            foreign key (idMissile)
                references missile (idMissile);

    alter table ammunitionFitting
        add constraint FKij9xicbw7lepyy25ixl7dr25q
            foreign key (idShipClass)
                references shipClass (idShipClass);

    alter table armor
        add constraint FKc1peuds75yuluxfttbe5omesp
            foreign key (idNamedTechLevel)
                references namedTechLevel (idNamedTechLevel);

    alter table armor
        add constraint FK10dhr7h3pkps3d7u22q2pwpgc
            foreign key (idCosts)
                references resourceDeposit (idResourceDeposit);

    alter table article
        add constraint FKhuuswjlsmm6e5n3l8ur3ba4dp
            foreign key (idBase)
                references article (idArticle);

    alter table article_articleRevisions
        add constraint FK68lt8xhflwv5e95q2quovun52
            foreign key (articleRevisions_idArticleRevision)
                references articleRevision (idArticleRevision);

    alter table article_articleRevisions
        add constraint FKcxraqd1wsqvn506o6elxd9e68
            foreign key (Article_idArticle)
                references article (idArticle);

    alter table articleLines
        add constraint FKd733msfpfqeysc4f75p4ed2e0
            foreign key (idArticleRevision)
                references articleRevision (idArticleRevision);

    alter table articleRevision
        add constraint FKd3v2o1xhbkle6k8es3g0s4wsr
            foreign key (idArticle)
                references article (idArticle);

    alter table articleRevision
        add constraint FK5wduechkafgqr5obeepid7mp4
            foreign key (idAuthor)
                references user (idUser);

    alter table battleReport
        add constraint FKktnc29kf1wrmrnbihghs9gmdp
            foreign key (idTick)
                references tick (idTick);

    alter table battleReport
       add constraint FKry6bc39fdk37dvtpfwtljucef
       foreign key (idPlanet)
       references planet (idPlanet);

    alter table battleReport
        add constraint FKr6smkmpvrxxus80181d1gwekl
            foreign key (idStarSystem)
                references starSystem (idStarSystem);

    alter table building
        add constraint FK9jureiokh5eus3dq46euhltxo
            foreign key (idTranslatableDescription)
                references translatable (idTranslatable);

    alter table building
        add constraint FKmqi7vubpnykxhu53hy5e7qri2
            foreign key (idTranslatableName)
                references translatable (idTranslatable);

    alter table building
        add constraint FK5vart3g8xv4gkgagwxxwyiuqi
            foreign key (idCosts)
                references resourceDeposit (idResourceDeposit);

    alter table building
        add constraint FKbp0gn3eiexsa5p6s20md9yfi7
            foreign key (idResearch)
                references research (idResearch);

    alter table colonization
        add constraint FK6kxulldcu79b6d6ecjr7pvpyw
            foreign key (idCosts)
                references resourceDeposit (idResourceDeposit);

    alter table colonization
        add constraint FKb2jrbwdwy0j8t2tggwtbf64ty
            foreign key (idTarget)
                references planet (idPlanet);

    alter table colonization
        add constraint FKrfuwalj6y19xvtebuy1q05pbt
            foreign key (idUser)
                references user (idUser);

    alter table construction
       add constraint FKah9i54pwsqd526a91s3ryci5o
       foreign key (idTickActivated)
       references tick (idTick);

    alter table construction
        add constraint FKlkteuncyf95jg9hhq28yefrcl
            foreign key (idBuilding)
                references building (idBuilding);

    alter table construction
        add constraint FKg139setxu2ng9hj6h7sgpyb9s
            foreign key (idPlanet)
                references planet (idPlanet);

    alter table counterMissileHit
        add constraint FKdc9r09hg3me03436ahneu2r65
            foreign key (idActor)
                references fleet (idFleet);

    alter table counterMissileHit
        add constraint FKksbgg1bvbgqrostw7xkhdo2lb
            foreign key (idMissile)
                references missile (idMissile);

    alter table counterMissileHit
        add constraint FKln1su9jkv2gcayljhc8x4vgem
            foreign key (idTarget)
                references fleet (idFleet);

    alter table counterMissileHits
        add constraint FK3iy5c5p8yauo6g7lx2egjlg5g
            foreign key (idCounterMissileHit)
                references counterMissileHit (idCounterMissileHit);

    alter table counterMissileHits
        add constraint FKcu6xd18vice5w5lmh1no2dk36
            foreign key (idBattleReport)
                references battleReport (idBattleReport);

    alter table electronicWarfare
        add constraint FKpwiw9e7b62krmlt84y1jbj5n8
            foreign key (idNamedTechLevel)
                references namedTechLevel (idNamedTechLevel);

    alter table electronicWarfare
        add constraint FKccj76id0r5pq3p7f4viriwdqf
            foreign key (idCosts)
                references resourceDeposit (idResourceDeposit);

    alter table eventRanking
       add constraint FKa03fqyutfi8ul5kr37ce7kra9
       foreign key (idUser)
       references user (idUser);

    alter table fleet
       add constraint FKkv8p42ny0lnkcqrrsvrpltsno
       foreign key (idTickActivated)
       references tick (idTick);

    alter table fleet
        add constraint FK5yy9whqh6562iaxuym0wrkjeq
            foreign key (idMove)
                references move (idMove);

    alter table fleet
       add constraint FKjn1r1mte3awql1sp7a2sehrsv
       foreign key (idPlanetLocation)
       references planet (idPlanet);

    alter table fleet
        add constraint FK7p0cvm6ul1v1w1vqcljs63i61
            foreign key (idStarSystemLocation)
                references starSystem (idStarSystem);

    alter table fleet
        add constraint FKjo66qwgl0a9bba5x7xq23fvok
            foreign key (idOwner)
                references user (idUser);

    alter table fleet
        add constraint FKckq55cmimjpois3mst803atuy
            foreign key (idResourceDeposit)
                references resourceDeposit (idResourceDeposit);

    alter table fleetSnapshot
        add constraint FK929r4p7vk0f3k3s4ocbt7h50e
            foreign key (idBattleReport)
                references battleReport (idBattleReport);

    alter table fleetSnapshot
        add constraint FK7m974jpjlnp7r615irb6nppcj
            foreign key (idFleet)
                references fleet (idFleet);

    alter table fleetSnapshot
        add constraint FKhr16a2b5d1q9yjjnc43holh2p
            foreign key (idOwner)
                references user (idUser);

    alter table flightPlan
       add constraint FK5r9d4uu2n4b4twymkxbmg7x7b
       foreign key (idPlanet)
       references planet (idPlanet);

    alter table flightPlan
       add constraint FKf4ew7t3sk8e7uid5vq61rm0qd
       foreign key (idStarSystem)
       references starSystem (idStarSystem);

    alter table flightPlan
       add constraint FK8pnr4eib22sc2tyr556x0750w
       foreign key (idMove)
       references move (idMove);

    alter table forum
        add constraint FKbd3cwb6yurr6utojembdwjiy1
            foreign key (idAlliance)
                references alliance (idAlliance);

    alter table forumMessage
        add constraint FKibroa7vxdgc63xasj1kcwrg4w
            foreign key (idUserAuthor)
                references user (idUser);

    alter table forumMessage
        add constraint FKh1a5uic7c3sdd84skyccc126q
            foreign key (idForumThread)
                references forumThread (idForumThread);

    alter table forumMessageRead
        add constraint FK12uxerbm5t8a7shn88fvvalbu
            foreign key (idForum)
                references forum (idForum);

    alter table forumMessageRead
        add constraint FKnf5e4g437o3l3hdg2ei0ywwe0
            foreign key (idForumMessage)
                references forumMessage (idForumMessage);

    alter table forumMessageRead
        add constraint FKtcsdm5ruogje2vjsy4oeok3md
            foreign key (idForumThread)
                references forumThread (idForumThread);

    alter table forumMessageRead
        add constraint FK2xe08nytb3qnnfmf906ynapx6
            foreign key (idUser)
                references user (idUser);

    alter table forumThread
        add constraint FKbqtbt77ebauj9krlwc3ak31us
            foreign key (idForum)
                references forum (idForum);

    alter table heatMap
       add constraint FKafi78glkfnkhp6l63bjifnuqr
       foreign key (idPlanet)
       references planet (idPlanet);

    alter table hitLog
        add constraint FK1pcr16gjbto8vd5g7v8hq14hw
            foreign key (idTarget)
                references warShip (idWarShip);

    alter table humanResources
        add constraint FKh3v7ra6rwylc7sofs1is80fb8
            foreign key (idResourceDeposit)
                references resourceDeposit (idResourceDeposit);

    alter table job
       add constraint FK9is567pcts10d2t0ciolkwt7p
       foreign key (idTickCompleted)
       references tick (idTick);

    alter table job
        add constraint FK7otfjvk4vhy0gt0m3hnyam6au
            foreign key (idBuilding)
                references building (idBuilding);

    alter table job
        add constraint FK9cgvto0bqandfg7ly93veyvc5
            foreign key (idFleet)
                references fleet (idFleet);

    alter table job
       add constraint FK7qelga4rbeqyxcbvr96lwcwh7
       foreign key (idFleetSnapshot)
       references fleetSnapshot (idFleetSnapshot);

    alter table job
        add constraint FKdno72guom99osq9f36eixsd87
            foreign key (idResearch)
                references research (idResearch);

    alter table job
        add constraint FK4ewa76co5drr08nptgdmax8d6
            foreign key (idFacility)
                references construction (idConstruction);

    alter table job
        add constraint FK3urqlpl2jmbxlfk4q88i9i5tb
            foreign key (idOwner)
                references user (idUser);

    alter table knownStarSystem
        add constraint FKayr540k7tyu8v1vuni31u2j17
            foreign key (idStarSystem)
                references starSystem (idStarSystem);

    alter table knownStarSystem
        add constraint FKtjhh901to46le5kkmsybuwdbb
            foreign key (idOwner)
                references user (idUser);

    alter table launcher
        add constraint FKt9vkee5hlkie2gu9lj8rmdal7
            foreign key (idNamedTechLevel)
                references namedTechLevel (idNamedTechLevel);

    alter table launcher
        add constraint FKpxevsicliklfnl6mycvl75sv9
            foreign key (idCosts)
                references resourceDeposit (idResourceDeposit);

    alter table lossesByHit
        add constraint FK8gwd5wcrghospusbhcyoffbpq
            foreign key (idFleet)
                references fleet (idFleet);

    alter table lossesByHit
        add constraint FKmxpfhc6uuo325u2u81tb2k2g0
            foreign key (idOwner)
                references user (idUser);

    alter table lossesByHit
        add constraint FK85p90t72v9he3a1iw7y0fhn05
            foreign key (idShipClass)
                references shipClass (idShipClass);

    alter table lossesByHit
        add constraint FKhtcg0ctdj5ie6fbabo8puvteu
            foreign key (idHitLog)
                references hitLog (idHitLog);

    alter table lossesByHit
        add constraint FK3o0d6nae9i5n6v33ake9fyvs8
            foreign key (idShipKillerHit)
                references shipKillerHit (idShipKillerHit);

    alter table messageThread
        add constraint FK1d5qqscr6uidy4lithqwkfcsb
            foreign key (idUserOne)
                references user (idUser);

    alter table messageThread
        add constraint FKlcfh5cw1nqv8howd22b9emwbf
            foreign key (idUserTwo)
                references user (idUser);

    alter table miningFactorsComposition
        add constraint FK7pw467msglkrl51uo8uu6v6l6
            foreign key (idMiningFactors)
                references miningFactors (idMiningFactors);

    alter table missile
        add constraint FK1as0lfywn7yqhs89vsuebxh4o
            foreign key (idNamedTechLevel)
                references namedTechLevel (idNamedTechLevel);

    alter table missile
        add constraint FK2y4rvixlct3ljky430p3bmwad
            foreign key (idCosts)
                references resourceDeposit (idResourceDeposit);

    alter table missileMovement
        add constraint FK31pwab7jyqugac58td2yh50ju
            foreign key (idActor)
                references fleet (idFleet);

    alter table missileMovement
        add constraint FKl9frhygmvi1n5d3sjchn19wrx
            foreign key (idTarget)
                references fleet (idFleet);

    alter table missileMovements
        add constraint FKa4ut542bvmk335w8ldma12g22
            foreign key (idMissileMovement)
                references missileMovement (idMissileMovement);

    alter table missileMovements
        add constraint FK4hmoghg3bi28t2pmfi3ea626u
            foreign key (idBattleReport)
                references battleReport (idBattleReport);

    alter table mission
       add constraint FKbvhlv330gufbb2p7aeeyagtu8
       foreign key (idActor)
       references user (idUser);

    alter table mission
       add constraint FKav28cevdimw8uypty1f3sgu3c
       foreign key (idTickStartedAt)
       references tick (idTick);

    alter table mission
       add constraint FKfjo5uacvj75iku0n27y8f781q
       foreign key (idTickStoppedAt)
       references tick (idTick);

    alter table mission
       add constraint FKgn39ow7ddmkf4bhyk50s47m1f
       foreign key (idTradeResource)
       references tradedResource (idTradedResource);

    alter table mission
       add constraint FKholrjg4864rt9j8qqs349b7ue
       foreign key (idPlanet)
       references planet (idPlanet);

    alter table missionItem
       add constraint FKjw6nl0yyik5mtyv227litad57
       foreign key (idTickCreatedAt)
       references tick (idTick);

    alter table missionItem
       add constraint FKpiatw8caol0quww65cxgqeayc
       foreign key (idTradeResource)
       references tradedResource (idTradedResource);

    alter table move
       add constraint FKhonj0ybwu1naypgp7b5d0cwip
       foreign key (idTickCompleted)
       references tick (idTick);

    alter table move
       add constraint FKpqyl1dnhe1gc67jbcsa6i10s9
       foreign key (idPlanetDestination)
       references planet (idPlanet);

    alter table move
        add constraint FKmcefsl29wdpj7xqe9790o0mch
            foreign key (idStarSystemDestination)
                references starSystem (idStarSystem);

    alter table move
        add constraint FKg65nht3m74odamnrqiv1cdyl6
            foreign key (idFleet)
                references fleet (idFleet);

    alter table move
       add constraint FKuoi0o5abd3i359pmc4idclrx
       foreign key (idFleetSnapshot)
       references fleetSnapshot (idFleetSnapshot);

    alter table move
       add constraint FK1us3my5u8r5mv1jupu0xw33fp
       foreign key (idPlanetOrigin)
       references planet (idPlanet);

    alter table move
        add constraint FK1y6v1eof54uci3p3b6mduv6sr
            foreign key (idStarSystemOrigin)
                references starSystem (idStarSystem);

    alter table move
        add constraint FKm0l3o2yx8pq8hu2bww8maoa98
            foreign key (idUser)
                references user (idUser);

    alter table move
       add constraint FK6f36ja23cxfke6ft2pu3j23fg
       foreign key (idTickStarted)
       references tick (idTick);

    alter table movementAction
        add constraint FK2fc6fy40a1twi3bedin6c2sr1
            foreign key (idActor)
                references fleet (idFleet);

    alter table movementActions
        add constraint FKlsfd21vc6bericbiwv6huwo
            foreign key (idMovementAction)
                references movementAction (idMovementAction);

    alter table movementActions
        add constraint FKjkre459vr8w45a9wxhrtofsfa
            foreign key (idBattleReport)
                references battleReport (idBattleReport);

    alter table namedTechLevel
        add constraint FKhmsd2ia4ak8y1bhaxrq9sap7w
            foreign key (idTranslatableDescription)
                references translatable (idTranslatable);

    alter table namedTechLevel
        add constraint FK47uiy14jdi17mluflq1obl3kw
            foreign key (idTranslatableName)
                references translatable (idTranslatable);

    alter table namedTechLevel
        add constraint FKcrxytyomlq85evr4ct20pvq3u
            foreign key (idResearch)
                references research (idResearch);

    alter table orbitalModule
       add constraint FK96s9g1xhrqa6mb0vdfptri16j
       foreign key (idTranslatableDescription)
       references translatable (idTranslatable);

    alter table orbitalModule
       add constraint FKjnv18sflhrm4m6dwn86ewb80
       foreign key (idTranslatableName)
       references translatable (idTranslatable);

    alter table orbitalModule
       add constraint FKjkhnt794699qh7ruud29r2tt
       foreign key (idCosts)
       references resourceDeposit (idResourceDeposit);

    alter table orbitalModule
       add constraint FK2nshm5a979bu2dnqk8wswlqcm
       foreign key (idResearch)
       references research (idResearch);

    alter table orbitalModuleJobElements
       add constraint FKhr85m1salx7enm27qj75gwg63
       foreign key (idOrbitalModule)
       references orbitalModule (idOrbitalModule);

    alter table orbitalModuleJobElements
       add constraint FKagpanf4seayijqw1ywrfs5ds4
       foreign key (idJob)
       references job (idJob);

    alter table orbitalStructure
       add constraint FKcnjc3xew6n67k9q8qhwkntbcb
       foreign key (idTickActivated)
       references tick (idTick);

    alter table orbitalStructure
       add constraint FKhd287pobvlknx1eo1b9rrix9x
       foreign key (idOrbitalModule)
       references orbitalModule (idOrbitalModule);

    alter table orbitalStructure
       add constraint FK4kmotox08ph1avbt2f98l4x5
       foreign key (idPlanet)
       references planet (idPlanet);

    alter table orbitalStructure
       add constraint FKnia95vlkjkyjdrqw3qwi1dt4e
       foreign key (idStarSystem)
       references starSystem (idStarSystem);

    alter table orbitalStructure
       add constraint FK89io2i6h4mwlhimons9paalqh
       foreign key (idOwner)
       references user (idUser);

    alter table orderedHitLog
        add constraint FKt4eji1de3lte0yql6naypaj9t
            foreign key (idHitLog)
                references hitLog (idHitLog);

    alter table orderedHitLog
        add constraint FKtbchtybhbygepe1yjf9mu0lwf
            foreign key (idShipKillerHit)
                references shipKillerHit (idShipKillerHit);

    alter table participatingFleets
        add constraint FKptc0phylec3318d12arsxd0j
            foreign key (idFleetSnapshot)
                references fleetSnapshot (idFleetSnapshot);

    alter table participatingFleets
        add constraint FKbp90ne9mn2vhmh9m7kinwjxki
            foreign key (idBattleReport)
                references battleReport (idBattleReport);

    alter table participatingUsers
        add constraint FK5dp3ok6qf3ohs6s1s7f11k34h
            foreign key (idUser)
                references user (idUser);

    alter table participatingUsers
       add constraint FK5cqqxdpvyx4jmlpmu1khk00sv
       foreign key (idSharedBattleReport)
       references sharedBattleReport (idSharedBattleReport);

    alter table passiveModule
        add constraint FK3q0uitju15ai7lhv7y7y61549
            foreign key (idTranslatableDescription)
                references translatable (idTranslatable);

    alter table passiveModule
        add constraint FK1kqbngjlngfx049m4t1hmyelt
            foreign key (idTranslatableName)
                references translatable (idTranslatable);

    alter table passiveModule
        add constraint FKrr0cmtk4xqkbtajq5s17apmsu
            foreign key (idCosts)
                references resourceDeposit (idResourceDeposit);

    alter table passiveModule
        add constraint FKdchcy45rswteu33yrgh80m8a9
            foreign key (idResearch)
                references research (idResearch);

    alter table planet
        add constraint FK6290orio72djryi0kktbn3esu
            foreign key (idMiningFactors)
                references miningFactors (idMiningFactors);

    alter table planet
        add constraint FKobjb6jgxji3jrrgoxy9r30uyc
            foreign key (idOwner)
                references user (idUser);

    alter table planet
        add constraint FK9cd80e9yxwnobejr9twlcknab
            foreign key (idResourceDeposit)
                references resourceDeposit (idResourceDeposit);

    alter table planet
        add constraint FK7ica1a9s7r5jy3dn3krk4hkfe
            foreign key (idResourceTransportationDelivery)
                references resourceDeposit (idResourceDeposit);

    alter table planet
        add constraint FKipb2odgmfpbftjlah8gxjh6fw
            foreign key (idResourceTransportationDemand)
                references resourceDeposit (idResourceDeposit);

    alter table planet
        add constraint FK2qd4p5ry3gaskjau8i2gutj0n
            foreign key (idStarSystem)
                references starSystem (idStarSystem);

    alter table propulsion
        add constraint FKlwu2dh95c5jr984f3l2ohr7s7
            foreign key (idNamedTechLevel)
                references namedTechLevel (idNamedTechLevel);

    alter table releasedVolley
        add constraint FK5phx9tuf726udgmc3oba1t80o
            foreign key (idActor)
                references fleet (idFleet);

    alter table releasedVolley
        add constraint FKox4m8c517vryrxaijolfco99m
            foreign key (idTarget)
                references fleet (idFleet);

    alter table releasesVolleys
        add constraint FKr0o8twcmeayvg09p39p71ktpf
            foreign key (idReleasedVolley)
                references releasedVolley (idReleasedVolley);

    alter table releasesVolleys
        add constraint FKn08s5o12up3n1n85d1vnhrk4y
            foreign key (idBattleReport)
                references battleReport (idBattleReport);

    alter table remainingShots
        add constraint FKapxtj9pueb5wn51s4bqdkpx2m
            foreign key (idMissile)
                references missile (idMissile);

    alter table remainingShots
        add constraint FKsu3q3bssdgu55ubnf1bbhr2ce
            foreign key (idWarshipHealthState)
                references warshipHealthState (idWarshipHealthState);

    alter table remainingShotsSnapshot
        add constraint FKcfct28ygeri834a9akuiafjqg
            foreign key (idMissile)
                references missile (idMissile);

    alter table remainingShotsSnapshot
        add constraint FKowghx8dytftgsrejrwv13qlbf
            foreign key (idWarshipHealthStateSnapshot)
                references warshipHealthStateSnapshot (idWarshipHealthStateSnapshot);

    alter table research
        add constraint FK1sxfrsxvrj2iaxi809oirhevj
            foreign key (idTranslatableDescription)
                references translatable (idTranslatable);

    alter table research
        add constraint FKibqicobq7dm63vf792kgmk5wj
            foreign key (idTranslatableName)
                references translatable (idTranslatable);

    alter table research
        add constraint FKni50te130dndarqgicsq3svhb
            foreign key (idCosts)
                references resourceDeposit (idResourceDeposit);

    alter table research
        add constraint FKch37eb44iv0ls442yu7usvvtp
            foreign key (unlockedThrough)
                references research (idResearch);

    alter table researchLevels
        add constraint FK8c7vw5t1ve4phgpfr6gwt3xj0
            foreign key (idResearch)
                references research (idResearch);

    alter table researchLevels
        add constraint FKh9xjvymkiqwygpem46iaj0j3v
            foreign key (idUser)
                references user (idUser);

    alter table resourcesDepositComposition
        add constraint FK6q26jn3ftmq2x638tsgi0aemy
            foreign key (idResourceDeposit)
                references resourceDeposit (idResourceDeposit);

    alter table rolePlaySetting
       add constraint FKhphq3ivotnm200m2l1h30rej4
       foreign key (idUser)
       references user (idUser);

    alter table sharedBattleReport
       add constraint FKyk7jxlttaktlvdhc231rwt80
       foreign key (idBattleReport)
       references battleReport (idBattleReport);

    alter table sharedWithAlliances
       add constraint FK3tp6tcx1ulbyfxa69lhh7ad1e
       foreign key (idAlliance)
       references alliance (idAlliance);

    alter table sharedWithAlliances
       add constraint FKn7nuh81nwugcay3yigdhmaqva
       foreign key (idSharedBattleReport)
       references sharedBattleReport (idSharedBattleReport);

    alter table sharedWithUsers
       add constraint FK3wt2lys7aqxisvvcxjhkh2iit
       foreign key (idUser)
       references user (idUser);

    alter table sharedWithUsers
       add constraint FKm9oj0tt8rij6u5r5mmfaexlwn
       foreign key (idSharedBattleReport)
       references sharedBattleReport (idSharedBattleReport);

    alter table shipClass
        add constraint FKouxjssb18x4jeutl5r1l0byeu
            foreign key (idArmor)
                references armor (idArmor);

    alter table shipClass
        add constraint FKfbii11hday9qcjpmi2i1k2611
            foreign key (idElectronicWarfare)
                references electronicWarfare (idElectronicWarfare);

    alter table shipClass
        add constraint FKovqcf68xgq4mm2n32sdoburq6
            foreign key (idOwner)
                references user (idUser);

    alter table shipClass
        add constraint FKr6026i6kn4nm4ss4h011nifks
            foreign key (idPredecessor)
                references shipClass (idShipClass);

    alter table shipClass
        add constraint FKdd7voavc2cml9rodxm6vnlaqq
            foreign key (idPropulsion)
                references propulsion (idPropulsion);

    alter table shipClass
        add constraint FKsa1b1j6ur2emh3jv7s0ft3nru
            foreign key (idSidewall)
                references sidewall (idSidewall);

    alter table shipClass
        add constraint FKnqevjdq10urslieg5r3peb5m3
            foreign key (idSuccessor)
                references shipClass (idShipClass);

    alter table shipKillerHit
        add constraint FKi72vdedsqsrgj93k3k80ei5sk
            foreign key (idActor)
                references fleet (idFleet);

    alter table shipKillerHit
        add constraint FK1d72qr1uwk27axl1yck2b2ux1
            foreign key (idTarget)
                references fleet (idFleet);

    alter table shipKillerHits
        add constraint FKmaalns2ubmv8018dgku4bv0qs
            foreign key (idShipKillerHit)
                references shipKillerHit (idShipKillerHit);

    alter table shipKillerHits
        add constraint FKsyea27u0x8dmybnb4r1qfsug0
            foreign key (idBattleReport)
                references battleReport (idBattleReport);

    alter table sidewall
        add constraint FKsotde61rdfb2dofbtlun2h1fn
            foreign key (idNamedTechLevel)
                references namedTechLevel (idNamedTechLevel);

    alter table sidewall
        add constraint FKlo0i3byallqh89wd535yrbs3l
            foreign key (idCosts)
                references resourceDeposit (idResourceDeposit);

    alter table supportFitting
        add constraint FKd2r1r3l1h9iehfvklg6tymj1o
            foreign key (idPassiveModule)
                references passiveModule (idPassiveModule);

    alter table supportFitting
        add constraint FK2rgk45foa8brx1onuwdxsodtr
            foreign key (idShipClass)
                references shipClass (idShipClass);

    alter table tradedResource
       add constraint FKnd475bqr8f5kdwemu760355mq
       foreign key (idTickCompleted)
       references tick (idTick);

    alter table tradedResource
       add constraint FK8i0ewnl7jdr7irx9pmilmq7ge
            foreign key (idBuyer)
                references user (idUser);

    alter table tradedResource
       add constraint FKcsfo7nv11frvg320e28xpoldi
       foreign key (idDestination)
       references planet (idPlanet);

    alter table tradedResource
       add constraint FKabd9jeuxd64c5r489056kpp17
       foreign key (idTickInitiated)
       references tick (idTick);

    alter table tradedResource
       add constraint FK5qw2mbtgucyq10mdhxc2ho72t
       foreign key (idTradeOffer)
       references tradeOffer (idTradeOffer);

    alter table tradeOffer
       add constraint FK48vicymhu5tup2co4k91e2urw
       foreign key (idOrigin)
       references planet (idPlanet);

    alter table tradeOffer
       add constraint FKi02ss97mli085wfdg9ngg49ja
            foreign key (idSeller)
                references user (idUser);

    alter table tradeOffer
       add constraint FKfs5vnnx3isy8srun4xll4rw0i
       foreign key (idTickInitiated)
                references tick (idTick);

    alter table transferredShips
       add constraint FKdi04kj2s8phv98b06t97b0g1b
       foreign key (idWarship)
       references warShip (idWarShip);

    alter table transferredShips
       add constraint FK8nikh4493iao57u0gjtht1bkn
       foreign key (idTransportJob)
       references transportJob (idTransportJob);

    alter table translation
        add constraint FK6y0ph13exuqqae7sowcvxac93
            foreign key (idTranslatable)
                references translatable (idTranslatable);

    alter table transportJob
       add constraint FK3p8d8qmsdvengg6kmvnesqipi
       foreign key (idTickCompleted)
       references tick (idTick);

    alter table transportJob
       add constraint FKq08wsniijayb6pwmfolhdphsl
       foreign key (idDestination)
       references planet (idPlanet);

    alter table transportJob
       add constraint FKc0d66ie3d1ungo75ft5ntsjj
       foreign key (idOrigin)
       references planet (idPlanet);

    alter table transportJob
       add constraint FK1t3cfowqftboj0yqntyki29fv
       foreign key (idOwner)
       references user (idUser);

    alter table transportJob
       add constraint FK9c2tnkoa6jp6j4hnqnpmstl16
       foreign key (idTickInitiated)
       references tick (idTick);

    alter table user
        add constraint FKd0120p7tkvssh9r8hldenpw1w
            foreign key (idAlliance)
                references alliance (idAlliance);

    alter table userMessage
        add constraint FKgo5irmd79mx2cg76wtaoaxbxa
            foreign key (idMessageThread)
                references messageThread (idMessageThread);

    alter table userMessage
        add constraint FK6xs6p78lala5xtd4eoe4xxrnv
            foreign key (idUserSender)
                references user (idUser);

    alter table userSetting
        add constraint FKq289093j0914b40umbxikg6fv
            foreign key (idUser)
                references user (idUser);

    alter table warShip
       add constraint FKm26l3odxqppbhlowov3tc64y9
       foreign key (idTickActivated)
       references tick (idTick);

    alter table warShip
        add constraint FK3kovfkp6003a62x5ff41h44hw
            foreign key (idFleet)
                references fleet (idFleet);

    alter table warShip
       add constraint FKr1fjudewjfngri3nq6axpbf5r
       foreign key (idMission)
       references mission (idMission);

    alter table warShip
       add constraint FKgxsukhuxoyaglnxcaawuyuh30
       foreign key (idMothball)
       references planet (idPlanet);

    alter table warShip
       add constraint FKeyf0wphylpn2gwbgwtvvspw4h
       foreign key (idTransportJob)
       references transportJob (idTransportJob);

    alter table warShip
        add constraint FKjr13y2u3qkka7d3npp9omwdoa
            foreign key (idShipClass)
                references shipClass (idShipClass);

    alter table warShip
        add constraint FKdywyvdwb0ovbd6oruywo13nyx
            foreign key (idShipyard)
                references planet (idPlanet);

    alter table warshipCapabilities
        add constraint FKcx1bs2mh0pk76hg4yvq57vy71
            foreign key (idWarshipHealthState)
                references warshipHealthState (idWarshipHealthState);

    alter table warshipCapabilitiesSnapshot
        add constraint FKeoahcn00mc9xyot7w9bqpcsof
            foreign key (idWarshipHealthStateSnapshot)
                references warshipHealthStateSnapshot (idWarshipHealthStateSnapshot);

    alter table warshipHealthState
        add constraint FK8n2fodpdy927lvcfqgsh1ejc8
            foreign key (idWarship)
                references warShip (idWarShip);

    alter table warshipHealthStateSnapshot
       add constraint FK11aatx339r9rwxxawx31i5ub1
       foreign key (idTickActivated)
       references tick (idTick);

    alter table warshipHealthStateSnapshot
        add constraint FKcjim226ew093h6wpualjjrodk
            foreign key (idFleetSnapshot)
                references fleetSnapshot (idFleetSnapshot);

    alter table warshipHealthStateSnapshot
        add constraint FKboga6909c5cwey1d6ung5i8we
            foreign key (idWarship)
                references warShip (idWarShip);

    alter table weapon
        add constraint FK65koohae6q4aft6chde2j86d9
            foreign key (idNamedTechLevel)
                references namedTechLevel (idNamedTechLevel);

    alter table weapon
        add constraint FK1rsb3ampiw8yjy8ngrget6ay
            foreign key (idCosts)
                references resourceDeposit (idResourceDeposit);

INSERT INTO user (username, gameUserRoles, userRole, dType)
VALUES ('Flashkid', 'FORUM_WRITE|WIKI_ADMIN|FORUM_READ|ALLIANCE_ADMIN', 'ADMIN', 'USER');
INSERT INTO userSetting (idUserSetting, email, password, createdAt, isEMailVerified, isLoginForbidden, noEMailWanted, receiveChangelogInfos, idUser) VALUE
                        (null, 'webmaster@battleforhonor.de',
                        '49675c186a6c1b1d10cb800e2792ebabd6abd8597bcef2fcaa99bfc813a6f1868b7dc91812ac66718c4fefd59daafa6a658901b7356b3b65fa5528419a93a7a4',
                        now(), 1, 0, 0, 1, 1);
INSERT INTO rolePlaySetting (idUser, shipNameTemplates) VALUE (1, 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE');

INSERT INTO user (username, dType) VALUES ('Defeated Opponent', 'NPC');
INSERT INTO rolePlaySetting (idUser, shipNameTemplates) VALUE (2, 'MANTICORE|HAVEN|ANDERMAN|SILESIA|SOLARIAN_LEAGUE');


DELIMITER //
CREATE TRIGGER forum_message_read_after_insert_user_trigger
    AFTER INSERT
    ON user
    FOR EACH ROW
BEGIN
    -- Insert into forumMessageRead for every existing forumMessage
    INSERT INTO forumMessageRead (idUser, idForum, idForumThread, idForumMessage)
    SELECT NEW.idUser, forumThread.idForum , forumThread.idForumThread, forumMessage.idForumMessage
    FROM forumMessage
             LEFT JOIN forumThread ON forumThread.idForumThread = forumMessage.idForumThread
             LEFT JOIN forum on forum.idForum = forumThread.idForum
                WHERE (forum.role IS NULL OR NEW.userRole LIKE CONCAT('%', forum.role, '%'))
                    OR (forum.idAlliance IS NULL OR NEW.idAlliance = forum.idAlliance);
END;
//
DELIMITER ;

DELIMITER //
CREATE TRIGGER forum_message_read_after_insert_forumMessage
AFTER INSERT ON forumMessage
FOR EACH ROW
BEGIN
    -- Insert into forumMessageRead for every existing user
    INSERT INTO forumMessageRead (idUser, idForum, idForumThread, idForumMessage)
    SELECT user.idUser, forumThread.idForum , forumThread.idForumThread, NEW.idForumMessage
    FROM user
        LEFT JOIN forumMessage ON forumMessage.idForumMessage = NEW.idForumMessage
        LEFT JOIN forumThread ON forumThread.idForumThread = forumMessage.idForumThread
        LEFT JOIN forum on forum.idForum = forumThread.idForum
                WHERE (forum.role IS NULL OR user.userRole LIKE CONCAT('%', forum.role, '%'))
                    OR (forum.idAlliance IS NULL OR user.idAlliance = forum.idAlliance);
END;
//
DELIMITER ;

insert into dbPatch values (null, now(), 'add traded resource', '0.1.2-1');
insert into dbPatch values (null, now(), 'rebalance buildings', '0.1.2-2');
insert into dbPatch values (null, now(), 'implement npc entity structure', '0.1.3-1');
insert into dbPatch values (null, now(), 'adapt defeated opponent as npc', '0.1.4-1');
insert into dbPatch values (null, now(), 'planned colonization', '0.1.5-1');
insert into dbPatch values (null, now(), 'heat map', '0.1.6-1');
insert into dbPatch values (null, now(), 'update warship caps', '0.1.6-2');
insert into dbPatch values (null, now(), 'piracy and missions wiki', '0.1.6-3');
insert into dbPatch values (null, now(), 'naval amendments', '0.1.6-4');
insert into dbPatch values (null, now(), 'profile pic', '0.1.7-1');
insert into dbPatch values (null, now(), 'drop persisted demand', '0.1.8-1');
insert into dbPatch values (null, now(), 'switch to job type', '0.1.9-1');
insert into dbPatch values (null, now(), 'add convoy mission', '0.1.10-1');
insert into dbPatch values (null, now(), 'add roleplay data', '0.1.11-1');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'add forum message trigger', '0.1.12-1');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'planetary mothball', '0.1.14-1');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'link wiki with tut', '0.1.14-2');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'increase pop output', '0.1.14-3');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'exchange ticks left to points', '0.1.14-4');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'reduce education level NONE', '0.1.14-5');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'completable fleet move', '0.1.15-1');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'drop heat from uncolonized planets', '0.1.15-2');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'fleet snap in job', '0.1.15-3');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'transport job', '0.1.15-4');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'tidy up wiki', '0.1.15-5');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'repair tonnage', '0.1.16-1');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'introduce orbital modules', '0.1.16-2');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'change tech names', '0.1.16-3');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'add tick to transport job', '0.1.16-4');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'introduce waypoints', '0.1.17-1');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'introduce game events', '0.1.17-2');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'more roleplay', '0.1.17-3');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'more more roleplay', '0.1.17-4');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'add indices', '0.1.17-5');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'add event participations', '0.1.17-6');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'replace operational cache', '0.1.17-7');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'share battle reports', '0.1.17-8');
INSERT INTO dbPatch VALUES (NULL, NOW(), 'introduce aligned ranges', '0.1.18-1');
