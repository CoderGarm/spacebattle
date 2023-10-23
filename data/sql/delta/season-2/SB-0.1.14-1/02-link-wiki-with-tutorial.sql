alter table article add column tutorialCategory varchar(255) after title;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'link wiki with tut', '0.1.14-2');
