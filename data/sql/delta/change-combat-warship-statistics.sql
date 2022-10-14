alter table hitLog drop column warshipHealthState;
alter table planet add column isMain bit not null default false;
-- todo set main planet by hand!