create table alliance
(
    idAlliance integer     not null auto_increment,
    code       varchar(30) not null,
    name       varchar(30) not null,
    primary key (idAlliance)
) engine = InnoDB;

create table building
(
    idBuilding               integer not null auto_increment,
    baseValue                integer not null,
    description              varchar(255),
    increasingFactorPerLevel decimal(19, 2),
    name                     varchar(30),
    resourceType             varchar(255),
    idCosts                  integer,
    idResearch               integer not null,
    primary key (idBuilding)
) engine = InnoDB;

create table construction
(
    idConstruction integer not null auto_increment,
    level          integer not null,
    idBuilding     integer not null,
    idPlanet       integer not null,
    primary key (idConstruction)
) engine = InnoDB;

create table fleet
(
    idFleet           integer      not null auto_increment,
    name              varchar(255) not null,
    idPlanet          integer,
    idStarsystem      integer,
    idOwner           integer      not null,
    idResourceDeposit integer,
    primary key (idFleet)
) engine = InnoDB;

create table fleetcomposition
(
    idFleet     integer not null,
    amount      integer,
    idShipClass integer not null,
    primary key (idFleet, idShipClass)
) engine = InnoDB;

create table hull
(
    idHull               integer      not null auto_increment,
    constructionCapacity integer      not null,
    description          varchar(255) not null,
    level                integer      not null,
    name                 varchar(30)  not null,
    idCosts              integer,
    idResearch           integer      not null,
    primary key (idHull)
) engine = InnoDB;

create table job
(
    idJob         integer        not null auto_increment,
    amountShips   integer,
    resourceType  varchar(255),
    targetLevel   integer,
    jobDoneAtZero decimal(19, 2) not null,
    idBuilding    integer,
    idResearch    integer,
    idShipclass   integer,
    idFacility    integer,
    idUser        integer        not null,
    primary key (idJob)
) engine = InnoDB;

create table module
(
    idModule    integer      not null auto_increment,
    description varchar(255) not null,
    level       integer      not null,
    moduleType  integer      not null,
    name        varchar(30)  not null,
    useCapacity integer      not null,
    value       integer      not null,
    idCosts     integer,
    idResearch  integer      not null,
    primary key (idModule)
) engine = InnoDB;

create table modulecomposition
(
    idShipclass integer not null,
    amount      integer,
    idModule    integer not null,
    primary key (idShipclass, idModule)
) engine = InnoDB;

create table move
(
    idMove             integer not null auto_increment,
    moveDoneAtZero     integer not null,
    idFleet            integer not null,
    idUser             integer not null,
    startIdPlanet      integer,
    startIdStarsystem  integer,
    targetIdPlanet     integer,
    targetIdStarsystem integer,
    primary key (idMove)
) engine = InnoDB;

create table planet
(
    idPlanet            integer     not null auto_increment,
    name                varchar(30) not null,
    xCoordinate         integer     not null,
    yCoordinate         integer     not null,
    idOwner             integer,
    idRescourcedeposits integer,
    idRescourcefactors  integer,
    idStarsystem        integer,
    primary key (idPlanet)
) engine = InnoDB;

create table rescources
(
    idResourceDeposit integer     not null,
    amount            decimal(19, 2),
    type              varchar(50) not null,
    primary key (idResourceDeposit, type)
) engine = InnoDB;

create table research
(
    idResearch      integer not null auto_increment,
    description     varchar(255),
    levelCap        integer not null,
    name            varchar(30),
    idCosts         integer,
    unlockedThrough integer,
    primary key (idResearch)
) engine = InnoDB;

create table resourceDeposit
(
    idResourceDeposit integer      not null auto_increment,
    subType           varchar(255) not null,
    primary key (idResourceDeposit)
) engine = InnoDB;

create table shipclass
(
    idShipclass integer      not null auto_increment,
    name        varchar(30)  not null,
    raceType    varchar(255) not null,
    idCosts     integer,
    idHull      integer      not null,
    idOwner     integer      not null,
    primary key (idShipclass)
) engine = InnoDB;

create table starsystem
(
    idStarsystem integer      not null auto_increment,
    name         varchar(255) not null,
    xCoordinate  integer      not null,
    yCoordinate  integer      not null,
    primary key (idStarsystem)
) engine = InnoDB;

create table systemcomposition
(
    idPlanet     integer not null,
    idStarsystem integer not null
) engine = InnoDB;

create table tick
(
    idTick     integer     not null auto_increment,
    tickEnds   datetime(6),
    tickStarts datetime(6) not null,
    primary key (idTick)
) engine = InnoDB;

create table unlockedResearch
(
    idUser     integer not null,
    level      integer,
    idResearch integer not null,
    primary key (idUser, idResearch)
) engine = InnoDB;

create table user
(
    idUser     integer      not null auto_increment,
    email      varchar(50)  not null,
    password   varchar(50)  not null,
    raceType   varchar(255) not null,
    username   varchar(30)  not null,
    idAlliance integer,
    primary key (idUser)
) engine = InnoDB;

alter table alliance
    add constraint UK_h7jfng3csi7xy8d1r3dqe07lo unique (code);

alter table alliance
    add constraint UK_7nuq4ufi5qsmpn1u6i8n2nxot unique (name);

alter table construction
    add constraint UK8c4oqqvxa4xpl5rmgoafhpc69 unique (idPlanet, idBuilding);

alter table job
    add constraint UK970jvv9t5arj4vbk44ygd9nrp unique (idFacility);

alter table planet
    add constraint UKdv40vo9ta4ir5vsolqropht2r unique (idStarsystem, idPlanet, xCoordinate, yCoordinate);

alter table shipclass
    add constraint UKhgcmmw0vvkvg6511jjpuw6bws unique (idOwner, name);

alter table starsystem
    add constraint UKt4lv9qo63hlsg9mbs7mddql8h unique (xCoordinate, yCoordinate);

alter table systemcomposition
    add constraint UK_sjq2umfqlsgivoxh49wwxgekb unique (idStarsystem);

alter table user
    add constraint UK_sb8bbouer5wak8vyiiy4pf2bx unique (username);

alter table building
    add constraint FK5vart3g8xv4gkgagwxxwyiuqi
        foreign key (idCosts)
            references resourceDeposit (idResourceDeposit);

alter table building
    add constraint FKbp0gn3eiexsa5p6s20md9yfi7
        foreign key (idResearch)
            references research (idResearch);

alter table construction
    add constraint FKlkteuncyf95jg9hhq28yefrcl
        foreign key (idBuilding)
            references building (idBuilding);

alter table construction
    add constraint FKg139setxu2ng9hj6h7sgpyb9s
        foreign key (idPlanet)
            references planet (idPlanet);

alter table fleet
    add constraint FKh6yguwrqsu1kah359o77c1b8h
        foreign key (idPlanet)
            references planet (idPlanet);

alter table fleet
    add constraint FK9p7bc2hmluuk682gxv6pfk1ve
        foreign key (idStarsystem)
            references starsystem (idStarsystem);

alter table fleet
    add constraint FKjo66qwgl0a9bba5x7xq23fvok
        foreign key (idOwner)
            references user (idUser);

alter table fleet
    add constraint FKckq55cmimjpois3mst803atuy
        foreign key (idResourceDeposit)
            references resourceDeposit (idResourceDeposit);

alter table fleetcomposition
    add constraint FK5gqfc5h0bjidbw1g27dm6p5vn
        foreign key (idShipClass)
            references shipclass (idShipclass);

alter table fleetcomposition
    add constraint FK8xjjuy4dvxqwloaaf4wge42qw
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

alter table job
    add constraint FK7otfjvk4vhy0gt0m3hnyam6au
        foreign key (idBuilding)
            references building (idBuilding);

alter table job
    add constraint FKdno72guom99osq9f36eixsd87
        foreign key (idResearch)
            references research (idResearch);

alter table job
    add constraint FKir289ws5tvge6hdcbbwtpw0gv
        foreign key (idShipclass)
            references shipclass (idShipclass);

alter table job
    add constraint FK4ewa76co5drr08nptgdmax8d6
        foreign key (idFacility)
            references construction (idConstruction);

alter table job
    add constraint FK2s6nln4ut6ul572h0flcpipx4
        foreign key (idUser)
            references user (idUser);

alter table module
    add constraint FKqxpwocsv3vwcws3g1yj7hpw8i
        foreign key (idCosts)
            references resourceDeposit (idResourceDeposit);

alter table module
    add constraint FK52hbj88ddt0mvoq1jv1rf5vk1
        foreign key (idResearch)
            references research (idResearch);

alter table modulecomposition
    add constraint FKcudmav236bb3nh619ye8gcp9p
        foreign key (idModule)
            references module (idModule);

alter table modulecomposition
    add constraint FKnclvtmoo3ftvkkaf45fpr8fpc
        foreign key (idShipclass)
            references shipclass (idShipclass);

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
    add constraint FKnyt7l8mioxwrvahwb9p86kbq0
        foreign key (startIdStarsystem)
            references starsystem (idStarsystem);

alter table move
    add constraint FKfhqgwhapcw4i2ydno4u1qlq77
        foreign key (targetIdPlanet)
            references planet (idPlanet);

alter table move
    add constraint FK7ttvnhp04l6htir72n61xkffj
        foreign key (targetIdStarsystem)
            references starsystem (idStarsystem);

alter table planet
    add constraint FKobjb6jgxji3jrrgoxy9r30uyc
        foreign key (idOwner)
            references user (idUser);

alter table planet
    add constraint FKefjg37nip5q3p67hxedb485n4
        foreign key (idRescourcedeposits)
            references resourceDeposit (idResourceDeposit);

alter table planet
    add constraint FKk3ha0bjd77n9thg57b6u1tue0
        foreign key (idRescourcefactors)
            references resourceDeposit (idResourceDeposit);

alter table planet
    add constraint FKn5q9kybjning6d8qag1rfutvr
        foreign key (idStarsystem)
            references starsystem (idStarsystem);

alter table rescources
    add constraint FK1g6ky1b4jtewtsbt384qpc5qi
        foreign key (idResourceDeposit)
            references resourceDeposit (idResourceDeposit);

alter table research
    add constraint FKni50te130dndarqgicsq3svhb
        foreign key (idCosts)
            references resourceDeposit (idResourceDeposit);

alter table research
    add constraint FKch37eb44iv0ls442yu7usvvtp
        foreign key (unlockedThrough)
            references research (idResearch);

alter table shipclass
    add constraint FK1ruka89wdh2fw4x3e3kasjg7l
        foreign key (idCosts)
            references resourceDeposit (idResourceDeposit);

alter table shipclass
    add constraint FKb1t7hnfcn96sywd3vyqv9xdyc
        foreign key (idHull)
            references hull (idHull);

alter table shipclass
    add constraint FKpx7092ewe0n8g9hu56dhqp7ip
        foreign key (idOwner)
            references user (idUser);

alter table systemcomposition
    add constraint FKcwpi16v4q8b3ifkguo88hsebd
        foreign key (idStarsystem)
            references planet (idPlanet);

alter table systemcomposition
    add constraint FK5hvyh64i3b88ui993bblu9heb
        foreign key (idPlanet)
            references starsystem (idStarsystem);

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
