alter table hitLog drop column if exists warshipHealthState;
alter table planet add column isMain bit not null default false;
-- todo set main planet by hand!