alter table job add column jobType varchar(255) after isRepairJob;
update job set jobType = 'REPAIR' where idFleet is not null and isRepairJob = true;
update job set jobType = 'CONSTRUCTION' where idFleet is not null and  isRepairJob = false;
alter table job drop column isRepairJob;

insert into dbPatch values (null, now(), 'switch to job type', '0.1.9-1');
