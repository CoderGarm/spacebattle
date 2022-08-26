alter table fleet add column isDeleted bit not null default false;
alter table sbdb.warShip add column isDeleted bit not null default false;