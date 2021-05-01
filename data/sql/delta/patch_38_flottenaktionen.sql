

-- TASK: repair fleetcomposition while it wasn't deleted when delete parent fleet
/*
 * Change fleet composition to delete orphans in @elementcollection.
 * Should work native with @elementcollection but it didn't.
 * Fun fact: it works with deleting ShipClass and also deleting moduleComposition-entries
 */

alter table fleetcomposition
        drop constraint FK8xjjuy4dvxqwloaaf4wge42qw;

alter table fleetcomposition
       add constraint FK8xjjuy4dvxqwloaaf4wge42qw
       foreign key (idFleet)
       references fleet (idFleet)
	   ON DELETE CASCADE;

-- TASK: add check constraint for "u cannot land where u start"
alter table move add check (startIdPlanet != targetIdPlanet);

-- TASK: repair move-fleet relationship
alter table fleet add column idMove integer after name;

alter table fleet
       add constraint UK_duhimx7ydhmssl7vqp5w29yx0 unique (idMove);

alter table fleet
       add constraint FK5yy9whqh6562iaxuym0wrkjeq
       foreign key (idMove)
       references move (idMove);


create table temp_move_transfer (idFleet integer, idMove integer);
insert into temp_move_transfer (idFleet, idMove) (select idFleet, idMove from move);

update fleet f set f.idMove = (select t.idMove from temp_move_transfer t where t.idFleet = f.idFleet);

drop table temp_move_transfer;