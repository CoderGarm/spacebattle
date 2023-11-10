update humanResources o set o.amount = (select sum(i.amount) from humanResources i where i.idResourceDeposit = o.idResourceDeposit and i.educationType != 'NONE') where o.educationType = 'NONE' and o.amount > 1000;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'reduce education level NONE', '0.1.14-5');
