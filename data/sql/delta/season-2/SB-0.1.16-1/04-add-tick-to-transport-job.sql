alter table transportJob add column idTickInitiated integer not null after idOwner;

# noinspection SqlWithoutWhere
update transportJob set idTickInitiated = 1;

alter table transportJob
   add constraint FK9c2tnkoa6jp6j4hnqnpmstl16
   foreign key (idTickInitiated)
   references tick (idTick);

INSERT INTO dbPatch VALUES (NULL, NOW(), 'add tick to transport job', '0.1.16-4');
