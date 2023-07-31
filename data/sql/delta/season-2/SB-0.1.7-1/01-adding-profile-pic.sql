alter table userSetting add column profilePic varchar(50) not null default 'perspective-dice-six-faces-random';
update userSetting set profilePic = 'disintegrate' where idUser = 1;

insert into dbPatch values (null, now(), 'profile pic', '0.1.7-1');
