-- todo delete alliance admin role
update user set idAlliance = null where idAlliance = 3;
delete from allianceApplication where idAlliance = 3;
-- todo delete forum messages, threads and reads
delete from forum where idAlliance = 3;
delete from alliance where idAlliance = 3;
