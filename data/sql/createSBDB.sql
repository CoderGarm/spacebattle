
    create table alignedFitting (
       idShipClass integer not null,
        amount integer not null,
        idWeapon integer,
        weaponAlignment varchar(255)
    ) engine=InnoDB;

    create table alliance (
       idAlliance integer not null auto_increment,
        code varchar(30) not null,
        name varchar(30) not null,
        primary key (idAlliance)
    ) engine=InnoDB;

    create table ammunitionFitting (
       idShipClass integer not null,
        idAmmunitionModule integer,
        amount integer not null
    ) engine=InnoDB;

    create table ammunitionModule (
       idAmmunitionModule integer not null auto_increment,
        description varchar(255) not null,
        effectValue integer not null,
        name varchar(30) not null,
        techLevel integer not null,
        useCapacity integer not null,
        idCosts integer not null,
        idResearch integer not null,
        primary key (idAmmunitionModule)
    ) engine=InnoDB;

    create table armor (
       idArmor integer not null auto_increment,
        description varchar(255) not null,
        effectValue integer not null,
        name varchar(30) not null,
        techLevel integer not null,
        useCapacity integer not null,
        idCosts integer not null,
        idResearch integer not null,
        primary key (idArmor)
    ) engine=InnoDB;

    create table building (
       idBuilding integer not null auto_increment,
        baseValue integer not null,
        description varchar(255),
        increasingFactorPerLevel decimal(19,2),
        name varchar(30),
        productionCategory varchar(255) not null,
        productionTarget varchar(255) not null,
        refinementSequence varchar(255),
        idCosts integer not null,
        idResearch integer not null,
        primary key (idBuilding)
    ) engine=InnoDB;

    create table colonization (
       idColonization integer not null auto_increment,
        doneAtZero integer not null,
        idCosts integer not null,
        idTarget integer not null,
        idUser integer not null,
        primary key (idColonization)
    ) engine=InnoDB;

    create table construction (
       idConstruction integer not null auto_increment,
        level integer not null,
        idBuilding integer not null,
        idPlanet integer not null,
        primary key (idConstruction)
    ) engine=InnoDB;

    create table electronicWarfare (
       idElectronicWarfare integer not null auto_increment,
        description varchar(255) not null,
        effectValue integer not null,
        name varchar(30) not null,
        techLevel integer not null,
        useCapacity integer not null,
        idCosts integer not null,
        idResearch integer not null,
        primary key (idElectronicWarfare)
    ) engine=InnoDB;

    create table fleet (
       idFleet integer not null auto_increment,
        name varchar(255) not null,
        idMove integer,
        idPlanet integer,
        idStarSystem integer,
        idOwner integer not null,
        idResourceDeposit integer,
        primary key (idFleet)
    ) engine=InnoDB;

    create table fleetComposition (
       idFleet integer not null,
        idWarShip integer not null,
        primary key (idFleet, idWarShip)
    ) engine=InnoDB;

    create table hull (
       idHull integer not null auto_increment,
        constructionCapacity integer not null,
        constructionCapacityBow integer not null,
        constructionCapacityBroadsides integer not null,
        constructionCapacityStern integer not null,
        description varchar(255) not null,
        hullType varchar(255) not null,
        level integer not null,
        name varchar(30) not null,
        idCosts integer,
        idResearch integer not null,
        primary key (idHull)
    ) engine=InnoDB;

    create table humanResources (
       idResourceDeposit integer not null,
        amount decimal(19, 0) not null,
        educationType varchar(50) not null,
        primary key (idResourceDeposit, educationType)
    ) engine=InnoDB;

    create table job (
       idJob integer not null auto_increment,
        amountShips integer,
        resourceType varchar(255),
        targetLevel integer,
        jobDoneAtZero decimal(19, 0) not null,
        idBuilding integer,
        idResearch integer,
        idShipClass integer,
        idFacility integer not null,
        idOwner integer not null,
        primary key (idJob),
        check ((idBuilding IS NOT NULL AND targetLevel IS NOT NULL) OR (idResearch IS NOT NULL AND targetLevel IS NOT NULL) OR (idShipClass IS NOT NULL AND amountShips IS NOT NULL))
    ) engine=InnoDB;

    create table knownStarSystem (
       idOwner integer not null,
        idStarSystem integer not null,
        primary key (idOwner, idStarSystem)
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

    create table move (
       idMove integer not null auto_increment,
        moveDoneAtZero integer not null,
        idFleet integer not null,
        idUser integer not null,
        startIdPlanet integer,
        startIdStarsystem integer,
        targetIdPlanet integer,
        targetIdStarsystem integer,
        primary key (idMove),
        check (startIdPlanet != targetIdPlanet)
    ) engine=InnoDB;

    create table passiveModule (
       idPassiveModule integer not null auto_increment,
        description varchar(255) not null,
        effectValue integer not null,
        name varchar(30) not null,
        techLevel integer not null,
        useCapacity integer not null,
        calculationType varchar(255) not null,
        supportType varchar(255) not null,
        idCosts integer not null,
        idResearch integer not null,
        primary key (idPassiveModule)
    ) engine=InnoDB;

    create table planet (
       idPlanet integer not null auto_increment,
        colonizedAt datetime(6),
        name varchar(30) not null,
        xCoordinate integer not null,
        yCoordinate integer not null,
        idMiningFactors integer not null,
        idOwner integer,
        idResourceDeposit integer,
        idStarSystem integer,
        primary key (idPlanet)
    ) engine=InnoDB;

    create table propulsion (
       idPropulsion integer not null auto_increment,
        description varchar(255) not null,
        effectValue integer not null,
        name varchar(30) not null,
        techLevel integer not null,
        useCapacity integer not null,
        ftlCapable bit not null,
        idCosts integer not null,
        idResearch integer not null,
        primary key (idPropulsion)
    ) engine=InnoDB;

    create table research (
       idResearch integer not null auto_increment,
        description varchar(255),
        levelCap integer not null,
        name varchar(30),
        idCosts integer,
        unlockedThrough integer,
        primary key (idResearch)
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

    create table shipClass (
       idShipClass integer not null auto_increment,
        isDeleted bit not null,
        name varchar(30) not null,
        idArmor integer,
        idElectronicWarfare integer,
        idHull integer not null,
        idOwner integer not null,
        idPredecessor integer,
        idPropulsion integer not null,
        idSidewall integer,
        idSuccessor integer,
        primary key (idShipClass)
    ) engine=InnoDB;

    create table sidewall (
       idSidewall integer not null auto_increment,
        description varchar(255) not null,
        effectValue integer not null,
        name varchar(30) not null,
        techLevel integer not null,
        useCapacity integer not null,
        idCosts integer not null,
        idResearch integer not null,
        primary key (idSidewall)
    ) engine=InnoDB;

    create table starSystem (
       idStarSystem integer not null auto_increment,
        name varchar(255) not null,
        xCoordinate integer not null,
        yCoordinate integer not null,
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

    create table unlockedResearch (
       idUser integer not null,
        level integer,
        idResearch integer not null,
        primary key (idUser, idResearch)
    ) engine=InnoDB;

    create table user (
       idUser integer not null auto_increment,
        email varchar(50) not null,
        password varchar(50) not null,
        username varchar(30) not null,
        idAlliance integer,
        primary key (idUser)
    ) engine=InnoDB;

    create table userMessage (
       idUserMessage integer not null auto_increment,
        message varchar(255),
        receivedAt datetime(6),
        sentAt datetime(6) not null,
        subject varchar(255) not null,
        idUserReceiver integer not null,
        idUserSender integer not null,
        primary key (idUserMessage)
    ) engine=InnoDB;

    create table warShip (
       idWarShip integer not null auto_increment,
        name varchar(255) not null,
        idFleet integer not null,
        idShipClass integer not null,
        idShipyard integer not null,
        primary key (idWarShip)
    ) engine=InnoDB;

    create table weapon (
       idWeapon integer not null auto_increment,
        description varchar(255) not null,
        effectValue integer not null,
        name varchar(30) not null,
        techLevel integer not null,
        useCapacity integer not null,
        alignmentType varchar(255) not null,
        damageType varchar(255) not null,
        effectiveRange integer not null,
        sideWallPenetration decimal(19, 5),
        weaponType varchar(255) not null,
        idCosts integer not null,
        idResearch integer not null,
        idAmmunitionModule integer,
        primary key (idWeapon)
    ) engine=InnoDB;

    alter table alliance 
       add constraint UK_h7jfng3csi7xy8d1r3dqe07lo unique (code);

    alter table alliance 
       add constraint UK_7nuq4ufi5qsmpn1u6i8n2nxot unique (name);

    alter table construction 
       add constraint CONSTRUCTION_UK unique (idPlanet, idBuilding);

    alter table fleet 
       add constraint UK_duhimx7ydhmssl7vqp5w29yx0 unique (idMove);

    alter table fleetComposition 
       add constraint UK_9aa4dwwkuicd7n10h7jn471am unique (idWarShip);

    alter table planet 
       add constraint PLANET_UK unique (idStarSystem, idPlanet, xCoordinate, yCoordinate);

    alter table shipClass 
       add constraint UK_4sgs4ew920mkttyjueq19n70q unique (idPredecessor);

    alter table shipClass 
       add constraint UK_kqyh4et3r89d2iy3w2sggpt90 unique (idSuccessor);

    alter table starSystem 
       add constraint COORDINATE_UK unique (xCoordinate, yCoordinate);

    alter table user 
       add constraint EMAIL_UK unique (email);

    alter table user 
       add constraint UK_sb8bbouer5wak8vyiiy4pf2bx unique (username);

    alter table alignedFitting 
       add constraint FKt6aos80sh8332mepbkuwmo98i 
       foreign key (idWeapon) 
       references weapon (idWeapon);

    alter table alignedFitting 
       add constraint FKgdp5e1ylgswr29e2d5b7uhib 
       foreign key (idShipClass) 
       references shipClass (idShipClass);

    alter table ammunitionFitting 
       add constraint FKmj2nxtrg5h9np8ugn7jre0v4f 
       foreign key (idAmmunitionModule) 
       references ammunitionModule (idAmmunitionModule);

    alter table ammunitionFitting 
       add constraint FKij9xicbw7lepyy25ixl7dr25q 
       foreign key (idShipClass) 
       references shipClass (idShipClass);

    alter table ammunitionModule 
       add constraint FKtc1t67bo67jgxojnt1r8w1hr3 
       foreign key (idCosts) 
       references resourceDeposit (idResourceDeposit);

    alter table ammunitionModule 
       add constraint FKi9oa4xlh6y6c8nd9e25c8jlbq 
       foreign key (idResearch) 
       references research (idResearch);

    alter table armor 
       add constraint FK10dhr7h3pkps3d7u22q2pwpgc 
       foreign key (idCosts) 
       references resourceDeposit (idResourceDeposit);

    alter table armor 
       add constraint FKrb3h67mjdni459t4j1y8b7sw5 
       foreign key (idResearch) 
       references research (idResearch);

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
       add constraint FKlkteuncyf95jg9hhq28yefrcl 
       foreign key (idBuilding) 
       references building (idBuilding);

    alter table construction 
       add constraint FKg139setxu2ng9hj6h7sgpyb9s 
       foreign key (idPlanet) 
       references planet (idPlanet);

    alter table electronicWarfare 
       add constraint FKccj76id0r5pq3p7f4viriwdqf 
       foreign key (idCosts) 
       references resourceDeposit (idResourceDeposit);

    alter table electronicWarfare 
       add constraint FKhr2adrrpeb3vshv11ajrgnkd7 
       foreign key (idResearch) 
       references research (idResearch);

    alter table fleet 
       add constraint FK5yy9whqh6562iaxuym0wrkjeq 
       foreign key (idMove) 
       references move (idMove);

    alter table fleet 
       add constraint FKh6yguwrqsu1kah359o77c1b8h 
       foreign key (idPlanet) 
       references planet (idPlanet);

    alter table fleet 
       add constraint FKtv4xu7x5k69o40m38jmf2lip 
       foreign key (idStarSystem) 
       references starSystem (idStarSystem);

    alter table fleet 
       add constraint FKjo66qwgl0a9bba5x7xq23fvok 
       foreign key (idOwner) 
       references user (idUser);

    alter table fleet 
       add constraint FKckq55cmimjpois3mst803atuy 
       foreign key (idResourceDeposit) 
       references resourceDeposit (idResourceDeposit);

    alter table fleetComposition 
       add constraint FK9v3ldmrdaym225uw9487vw2ek 
       foreign key (idWarShip) 
       references warShip (idWarShip);

    alter table fleetComposition 
       add constraint FKfk2nrx1fso8buadj6v4sw1j04 
       foreign key (idFleet) 
       references fleet (idFleet);

    alter table hull 
       add constraint FK65udyybp7syxvga5evxn8olhc 
       foreign key (idCosts) 
       references resourceDeposit (idResourceDeposit);

    alter table hull 
       add constraint FK4hpf1pawl0wynjx9kdg74opea 
       foreign key (idResearch) 
       references research (idResearch);

    alter table humanResources 
       add constraint FKh3v7ra6rwylc7sofs1is80fb8 
       foreign key (idResourceDeposit) 
       references resourceDeposit (idResourceDeposit);

    alter table job 
       add constraint FK7otfjvk4vhy0gt0m3hnyam6au 
       foreign key (idBuilding) 
       references building (idBuilding);

    alter table job 
       add constraint FKdno72guom99osq9f36eixsd87 
       foreign key (idResearch) 
       references research (idResearch);

    alter table job 
       add constraint FKsevbhc9015r9wmqvojq1dbsen 
       foreign key (idShipClass) 
       references shipClass (idShipClass);

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

    alter table miningFactorsComposition 
       add constraint FK7pw467msglkrl51uo8uu6v6l6 
       foreign key (idMiningFactors) 
       references miningFactors (idMiningFactors);

    alter table move 
       add constraint FKg65nht3m74odamnrqiv1cdyl6 
       foreign key (idFleet) 
       references fleet (idFleet);

    alter table move 
       add constraint FKm0l3o2yx8pq8hu2bww8maoa98 
       foreign key (idUser) 
       references user (idUser);

    alter table move 
       add constraint FKa1bs79m293x3ok5ose0jli0r9 
       foreign key (startIdPlanet) 
       references planet (idPlanet);

    alter table move 
       add constraint FK66wwxap7hrv54faje90tmrbb0 
       foreign key (startIdStarsystem) 
       references starSystem (idStarSystem);

    alter table move 
       add constraint FKfhqgwhapcw4i2ydno4u1qlq77 
       foreign key (targetIdPlanet) 
       references planet (idPlanet);

    alter table move 
       add constraint FKr8obp03f86v1f41icg4xro1rl 
       foreign key (targetIdStarsystem) 
       references starSystem (idStarSystem);

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
       add constraint FK2qd4p5ry3gaskjau8i2gutj0n 
       foreign key (idStarSystem) 
       references starSystem (idStarSystem);

    alter table propulsion 
       add constraint FKqjsvyhjc6w21niim4aeptpm85 
       foreign key (idCosts) 
       references resourceDeposit (idResourceDeposit);

    alter table propulsion 
       add constraint FK7rr2gvpcbjjhl9tuxe6c50v5q 
       foreign key (idResearch) 
       references research (idResearch);

    alter table research 
       add constraint FKni50te130dndarqgicsq3svhb 
       foreign key (idCosts) 
       references resourceDeposit (idResourceDeposit);

    alter table research 
       add constraint FKch37eb44iv0ls442yu7usvvtp 
       foreign key (unlockedThrough) 
       references research (idResearch);

    alter table resourcesDepositComposition 
       add constraint FK6q26jn3ftmq2x638tsgi0aemy 
       foreign key (idResourceDeposit) 
       references resourceDeposit (idResourceDeposit);

    alter table shipClass 
       add constraint FKouxjssb18x4jeutl5r1l0byeu 
       foreign key (idArmor) 
       references armor (idArmor);

    alter table shipClass 
       add constraint FKfbii11hday9qcjpmi2i1k2611 
       foreign key (idElectronicWarfare) 
       references electronicWarfare (idElectronicWarfare);

    alter table shipClass 
       add constraint FKgkjpsgpvfaupqxr7cv9nhc9ai 
       foreign key (idHull) 
       references hull (idHull);

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

    alter table sidewall 
       add constraint FKlo0i3byallqh89wd535yrbs3l 
       foreign key (idCosts) 
       references resourceDeposit (idResourceDeposit);

    alter table sidewall 
       add constraint FK693a9gix6ifpkiop612tghdy0 
       foreign key (idResearch) 
       references research (idResearch);

    alter table supportFitting 
       add constraint FKd2r1r3l1h9iehfvklg6tymj1o 
       foreign key (idPassiveModule) 
       references passiveModule (idPassiveModule);

    alter table supportFitting 
       add constraint FK2rgk45foa8brx1onuwdxsodtr 
       foreign key (idShipClass) 
       references shipClass (idShipClass);

    alter table unlockedResearch 
       add constraint FKc4x693khs2f17y0jjfb625o51 
       foreign key (idResearch) 
       references research (idResearch);

    alter table unlockedResearch 
       add constraint FKigikopnlfckk76o2yo3utm5s9 
       foreign key (idUser) 
       references user (idUser);

    alter table user 
       add constraint FKd0120p7tkvssh9r8hldenpw1w 
       foreign key (idAlliance) 
       references alliance (idAlliance);

    alter table userMessage 
       add constraint FK5mctsyrt040xp1wgghtk6lxxr 
       foreign key (idUserReceiver) 
       references user (idUser);

    alter table userMessage 
       add constraint FK6xs6p78lala5xtd4eoe4xxrnv 
       foreign key (idUserSender) 
       references user (idUser);

    alter table warShip 
       add constraint FK3kovfkp6003a62x5ff41h44hw 
       foreign key (idFleet) 
       references fleet (idFleet);

    alter table warShip 
       add constraint FKjr13y2u3qkka7d3npp9omwdoa 
       foreign key (idShipClass) 
       references shipClass (idShipClass);

    alter table warShip 
       add constraint FKdywyvdwb0ovbd6oruywo13nyx 
       foreign key (idShipyard) 
       references planet (idPlanet);

    alter table weapon 
       add constraint FK1rsb3ampiw8yjy8ngrget6ay 
       foreign key (idCosts) 
       references resourceDeposit (idResourceDeposit);

    alter table weapon 
       add constraint FKo22n18dgjpraqosj7nkamrnvb 
       foreign key (idResearch) 
       references research (idResearch);

    alter table weapon 
       add constraint FKpteqae0l9alndx95maj9fkhvj 
       foreign key (idAmmunitionModule) 
       references ammunitionModule (idAmmunitionModule);
