

update user set username = 'x' where idUser = 25;
update user set username = 'The95thRifleman' where idUser = 26;
update userSetting set password = (select password from userSetting where idUser = 25) where idUser = 26;
