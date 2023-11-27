
# noinspection SqlWithoutWhere
update weapon set tonnage = concat(effectValue, ' T');

update weapon set effectValue = 1 where idWeapon < 4;
update weapon set effectValue = 40 where idWeapon = 5;

update weapon set effectValue = 65 where idWeapon = 6;
update weapon set effectValue = 80 where idWeapon = 7;
update weapon set effectValue = 90 where idWeapon = 8;
update weapon set effectValue = 110 where idWeapon = 9;
update weapon set effectValue = 160 where idWeapon = 10;
update weapon set effectValue = 200 where idWeapon = 11;
update weapon set effectValue = 350 where idWeapon = 12;
update weapon set effectValue = 400 where idWeapon = 13;
update weapon set effectValue = 500 where idWeapon = 14;
update weapon set effectValue = 850 where idWeapon = 15;
update weapon set effectValue = 1100 where idWeapon = 16;
update weapon set effectValue = 1450 where idWeapon = 17;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'repair tonnage', '0.1.16-1');
