
    create table heatMap (
       idHeatMap integer not null auto_increment,
        heat integer not null,
        missionType varchar(255) not null,
        idOwner integer not null,
        primary key (idHeatMap)
    ) engine=InnoDB;

   alter table heatMap
       add constraint FK1uwloy48staea5okd5x30c0qt
       foreign key (idOwner)
       references user (idUser);

INSERT INTO heatMap (heat, missionType, idOwner) SELECT 1, 'ACTIVE_PIRATE', idUser FROM user WHERE dType = 'USER';
# noinspection SqlWithoutWhere
update heatMap h set heat = (select count(f.idFleet) from fleet f where f.isDeleted = false and f.isOperational = true and h.idOwner = f.idOwner);

insert into dbPatch values (null, now(), 'heat map', '0.1.6-1');
