
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
    price bigint not null,
    resourceType varchar(255) not null,
    idOrigin integer,
    idSeller integer,
    idTickInitiated integer not null,
    primary key (idTradeOffer)
) engine=InnoDB;


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

ALTER TABLE job RENAME COLUMN jobDoneAtZero TO ticksLeft;
ALTER TABLE job DROP FOREIGN KEY FKe2jgcwt8phugfp1aj5bu76132;
ALTER TABLE job RENAME COLUMN idTick TO idTickCompleted;
alter table job add constraint FK9is567pcts10d2t0ciolkwt7p foreign key (idTickCompleted) references tick (idTick);

insert into dbPatch values (null, now(), 'add traded resource', '0.1.1-1');
