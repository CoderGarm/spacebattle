
alter table user add column dType varchar(31) not null default 'USER';

alter table user drop constraint EMAIL_UK;

alter table userSetting add column email varchar(50) not null;
alter table userSetting add column password varchar(255) not null;
alter table userSetting add column createdAt datetime(6) not null;

update userSetting s set
    password = (select password from user u where u.idUser = s.idUser),
    email = (select email from user u where u.idUser = s.idUser),
    createdAt = (select createdAt from user u where u.idUser = s.idUser);
alter table userSetting add constraint EMAIL_UK unique (email);

alter table user drop column email;
alter table user drop column password;
alter table user drop column createdAt;

insert into dbPatch values (null, now(), 'implement npc entity structure', '0.1.3-1');
