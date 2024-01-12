alter table alliance add column leftBottom longtext after createdAt;
alter table alliance add column leftUpper longtext after leftBottom;
alter table alliance add column rightBottom longtext after leftUpper;
alter table alliance add column rightUpper longtext after rightBottom;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'more more roleplay', '0.1.17-4');
