update user set dType = 'NPC' where idUser = 2;
delete from userSetting where idUser = 2;

alter table userSetting
       add constraint UK_de78bv8lgkrdwqxfpqr8k3wfu unique (idUser);

insert into dbPatch values (null, now(), 'adapt defeated opponent as npc', '0.1.4-1');
