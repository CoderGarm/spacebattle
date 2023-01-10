create table article (
   idArticle integer not null auto_increment,
    langCode varchar(255) not null,
    title varchar(255) not null,
    wikiCategory varchar(255) not null,
    idBase integer,
    primary key (idArticle)
) engine=InnoDB;


alter table article
   add constraint FKhuuswjlsmm6e5n3l8ur3ba4dp
   foreign key (idBase)
   references article (idArticle);

create table article_articleRevisions (
   Article_idArticle integer not null,
    articleRevisions_idArticleRevision integer not null,
    primary key (Article_idArticle, articleRevisions_idArticleRevision)
) engine=InnoDB;

create table articleLines (
   idArticleRevision integer not null,
    content varchar(255),
    deltaType varchar(255) not null,
    lineNo integer not null
) engine=InnoDB;

create table articleRevision (
   idArticleRevision integer not null auto_increment,
    version integer not null,
    idArticle integer not null,
    idAuthor integer not null,
    primary key (idArticleRevision)
) engine=InnoDB;

alter table article_articleRevisions
    add constraint UK_oq8mr3fxh6t655kgme0wxg7pa unique (articleRevisions_idArticleRevision);

alter table article_articleRevisions
   add constraint FK68lt8xhflwv5e95q2quovun52
   foreign key (articleRevisions_idArticleRevision)
   references articleRevision (idArticleRevision);

alter table article_articleRevisions
   add constraint FKcxraqd1wsqvn506o6elxd9e68
   foreign key (Article_idArticle)
   references article (idArticle);

alter table articleLines
   add constraint FKd733msfpfqeysc4f75p4ed2e0
   foreign key (idArticleRevision)
   references articleRevision (idArticleRevision);

alter table articleRevision
   add constraint FKd3v2o1xhbkle6k8es3g0s4wsr
   foreign key (idArticle)
   references article (idArticle);

alter table articleRevision
   add constraint FK5wduechkafgqr5obeepid7mp4
   foreign key (idAuthor)
   references user (idUser);

INSERT INTO article (langCode, title, wikiCategory, idBase) VALUES ('en', 'Welcome to the battlefield', 'WELCOME_MESSAGE', null);
INSERT INTO article (langCode, title, wikiCategory, idBase) VALUES ('de', 'Willkommen auf dem Schlachtfeld', 'WELCOME_MESSAGE', 1);
INSERT INTO article (langCode, title, wikiCategory, idBase) VALUES ('en', 'Population', 'GAME_MECHANICS', null);
INSERT INTO article (langCode, title, wikiCategory, idBase) VALUES ('en', 'Combat and missions', 'GAME_MECHANICS', null);

INSERT INTO articleRevision (version, idArticle, idAuthor) VALUES (1, 1, 1);
INSERT INTO articleRevision (version, idArticle, idAuthor) VALUES (1, 2, 1);
INSERT INTO articleRevision (version, idArticle, idAuthor) VALUES (1, 3, 1);
INSERT INTO articleRevision (version, idArticle, idAuthor) VALUES (1, 4, 1);

INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '# Welcome to the battlefield', 'INSERT', 0);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '---', 'INSERT', 1);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 2);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '**Battle for honor** is a classic, tick-based 4X browsergame.  ', 'INSERT', 3);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'Start with your first colony to _Explore_ the galaxy. _Exploit_ the resources of your star system and _Expand_ your', 'INSERT', 4);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 5);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'Send your diplomats to make friends, fight pirates and discover your enemies.  ', 'INSERT', 6);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'Build a superior fleet and _Exterminate_ the latter. Take control of their systems and planets.', 'INSERT', 7);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 8);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '### Background', 'INSERT', 9);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 10);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'This game is based on the space opera _Honor Harrington_ by David Weber and Friends.  ', 'INSERT', 11);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'Besides the strong recommendation to read the opera it could be useful but is not necessary to understand the principles', 'INSERT', 12);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 13);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'Battle for Honor is tick-based and a tick starts and ends at midnight - obviously not at the same days.  ', 'INSERT', 14);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'The consequences of your actions will be realized at the end of a tick.', 'INSERT', 15);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 16);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '---', 'INSERT', 17);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '## Remarks', 'INSERT', 18);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 19);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'This game is in an early stage and under development.  ', 'INSERT', 20);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'Not everything will work as expected and something should work but it won''t.', 'INSERT', 21);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 22);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'In these cases please inform the developers.', 'INSERT', 23);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 24);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '---', 'INSERT', 25);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '## Important notice', 'INSERT', 26);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 27);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, 'You are welcome in every case, but please be aware that this is a hobby project.', 'INSERT', 28);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (1, '', 'INSERT', 29);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '# Willkommen auf dem Schlachtfeld', 'INSERT', 0);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '---', 'INSERT', 1);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '', 'INSERT', 2);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '**Battle for Honor** ist ein klassisches, tickbasiertes 4X-Browsergame.  ', 'INSERT', 3);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Beginne mit deiner ersten Kolonie, um die Galaxie zu _erkunden_. _Beute_ die Ressourcen deines Sternensystems aus und _erweitere_ dein Imperium.', 'INSERT', 4);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '', 'INSERT', 5);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Schicke deine Diplomaten los, um Freunde zu finden, Piraten zu bekämpfen und Ihre Feinde zu erkennen.  ', 'INSERT', 6);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Baue eine überlegene Flotte und _vernichte_ sie. Übernimm die Kontrolle über ihre Systeme und Planeten.', 'INSERT', 7);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '', 'INSERT', 8);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '### Hintergrund', 'INSERT', 9);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '', 'INSERT', 10);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Dieses Spiel basiert auf der Space Opera _Honor Harrington_ von David Weber and Friends.  ', 'INSERT', 11);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Abgesehen von der dringenden Empfehlung, die Reihe zu lesen, kann es nützlich sein, ist aber nicht notwendig, um die Prinzipien des Spiels zu verstehen.', 'INSERT', 12);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '', 'INSERT', 13);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Battle for Honor ist tickbasiert und ein Tick beginnt und endet um Mitternacht – offensichtlich nicht an denselben Tagen.  ', 'INSERT', 14);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Die Konsequenzen deiner Handlungen werden am Ende eines Ticks realisiert.', 'INSERT', 15);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '', 'INSERT', 16);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '---', 'INSERT', 17);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '## Bemerkungen', 'INSERT', 18);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '', 'INSERT', 19);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Dieses Spiel befindet sich in einem frühen Stadium und befindet sich in der Entwicklung.  ', 'INSERT', 20);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Nicht alles wird wie erwartet funktionieren und etwas sollte funktionieren, wird es möglicherweise aber nicht.', 'INSERT', 21);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '', 'INSERT', 22);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Bitte informiere die in diesen Fällen die Entwickler.', 'INSERT', 23);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '', 'INSERT', 24);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '---', 'INSERT', 25);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '## Wichtiger Hinweis', 'INSERT', 26);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, '', 'INSERT', 27);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (2, 'Du bist in jedem Fall willkommen, aber bitte beachte, dass dies ein Hobbyprojekt ist.', 'INSERT', 28);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '# Population', 'INSERT', 0);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '---', 'INSERT', 1);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '## Basic concept', 'INSERT', 2);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 3);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'Besides the fact that the population represents humans it will be used as a special resource.  ', 'INSERT', 4);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'It has multiple aspects which are not present for other resources.', 'INSERT', 5);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 6);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '### Population development', 'INSERT', 7);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 8);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'Your inhabitants will be born, they grew up and somewhen they will educate themself if you provide the opportunity.  ', 'INSERT', 9);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'After they are born and has grown up, they will visit a school.  ', 'INSERT', 10);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'For obvious reasons they can''t be part of the workforce.', 'INSERT', 11);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 12);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'If they are educated enough they can be part of the workforce, if there are enough jobs present.', 'INSERT', 13);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 14);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'Every building or warship will create jobs following their demand.  ', 'INSERT', 15);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'If there are not enough people with the requested education level present, they notice that there are vacancies and try to achive the needed education level to work there.', 'INSERT', 16);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 17);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '### Operationals', 'INSERT', 18);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 19);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'This is strongly related to the concept of operationals.  ', 'INSERT', 20);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'A new building level or warship will start inoperational and as soon as their full requirement of workforce is satisfied, they will begin to operate.', 'INSERT', 21);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 22);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '### Warships', 'INSERT', 23);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 24);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'Every warship of a fleet must be commissioning in order to use the fleet.', 'INSERT', 25);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 26);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '### Colonization', 'INSERT', 27);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, '', 'INSERT', 28);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (3, 'A colonization can currently not be planned. To colonize another planet the full demand of people must be available.', 'INSERT', 29);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '# Combat and missions', 'INSERT', 0);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 1);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, 'Every fleet will have a mission.', 'INSERT', 2);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 3);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, 'The mission types are a way to define the best outcome of the fleet clash for the fleet''s owner. The mission types will be the base of the decision-making for several combat', 'INSERT', 4);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, 'phases.', 'INSERT', 5);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 6);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '## Mission types', 'INSERT', 7);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 8);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '1. seek and destroy', 'INSERT', 9);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - the simplest way to attack somebody directly', 'INSERT', 10);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - the fleet tries to outmaneuver it''s opponent in a way that the opponent will gets the maximized damage', 'INSERT', 11);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - the fleet will attack a hidden fleet on detection', 'INSERT', 12);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 13);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '   ###### allowed rules of entry into combat: [Protective](#protective), [Offensive](#offensive)', 'INSERT', 14);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 15);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 16);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '2. hide and sneak', 'INSERT', 17);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - hide from all opponents in the area', 'INSERT', 18);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - the fleet tries to stay in the area without being detected', 'INSERT', 19);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - the fleet will avoid contact and tries to escape instead of initiating a combat on detection', 'INSERT', 20);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 21);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '   ###### allowed rules of entry into combat: [Defensive](#defensive), [Offensive](#offensive)', 'INSERT', 22);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 23);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 24);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '3. peaceful presence', 'INSERT', 25);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - show presence but stay peaceful', 'INSERT', 26);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - in the fleet owner''s or allied system it switches implicit to **seek and destroy**', 'INSERT', 27);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - in a foreign system the fleet will tries to evade and escape', 'INSERT', 28);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 29);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '   ###### allowed rules of entry into combat: [Assertive](#assertive), [Protective](#protective)', 'INSERT', 30);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 31);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '## Code of conduct or rules of entry into combat', 'INSERT', 32);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 33);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, 'The code of conduct defines how the captain or flag officer will present her or his fleet to possible opponents.', 'INSERT', 34);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 35);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, 'This will specify that the fleet will try to escape from the battlefield or will try to defeat the opponent.', 'INSERT', 36);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 37);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '###### Defensive', 'INSERT', 38);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 39);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '- the flag officer will try to evade any combat and **escape** if the opponent will start a fight', 'INSERT', 40);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 41);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '###### Assertive', 'INSERT', 42);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 43);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '- the flag officer will start an argument but not a fight', 'INSERT', 44);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '- the flag officer will try to evade any combat if the opponent will tries to start a fight', 'INSERT', 45);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 46);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '###### Protective', 'INSERT', 47);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 48);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '- the flag officer will start an argument but not a fight', 'INSERT', 49);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '- the flag officer will fight if the opponent start it', 'INSERT', 50);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 51);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '###### Offensive', 'INSERT', 52);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 53);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '- the flag officer will start an argument and, if needed, the fight', 'INSERT', 54);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 55);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '# Combat system', 'INSERT', 56);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 57);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '1. ###### enter cage outside the weapon range', 'INSERT', 58);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    1. [fleet movement](#fleet-movement)', 'INSERT', 59);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    2. [missile movement](#missile-movement)', 'INSERT', 60);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    3. [incoming weapon fire](#incoming-weapon-fire)', 'INSERT', 61);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    4. [fire weapons](#fire-weapons)', 'INSERT', 62);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 63);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - **repeat until one side has won**', 'INSERT', 64);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '2. ###### leaving cage', 'INSERT', 65);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 66);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '## Phase definition:', 'INSERT', 67);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 68);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '1. ### fleet movement', 'INSERT', 69);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 70);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '   ###### move fleets by initiative - the better, the later', 'INSERT', 71);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 72);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    1. determine initiative for all moving fleets', 'INSERT', 73);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        1. detect the best [movement motivation](#movement) to suit the [mission type](#mission-types)', 'INSERT', 74);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '            - the fleet detects incoming weapon fire, the opponent''s distance and movement to state the situation', 'INSERT', 75);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        2. perform movement', 'INSERT', 76);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 77);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        - **repeat until every fleet has moved**', 'INSERT', 78);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 79);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 80);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '2. ### missile movement', 'INSERT', 81);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    1. detect distance of salvo to the target', 'INSERT', 82);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    2. detect if missiles are in **ELOKA**-range', 'INSERT', 83);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        1. reduce salvo which are in **ELOKA**-range', 'INSERT', 84);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    3. detect if missiles are in **COUNTER MISSILE**-range', 'INSERT', 85);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        1. reduce salvo which are in **COUNTER MISSILE**-range', 'INSERT', 86);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    4. move salvo towards their target', 'INSERT', 87);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 88);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - **repeat until every salvo has moves**', 'INSERT', 89);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 90);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 91);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '3. ### incoming weapon fire', 'INSERT', 92);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    1. separate incoming weapon hits by weapon type', 'INSERT', 93);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        1. detect chance to hit by fire control solution and target''s last movement', 'INSERT', 94);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '            - **BEAMS**', 'INSERT', 95);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '                1. all **BEAMs** are in range for a direct hit to their target', 'INSERT', 96);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '                2. reduce all BEAMS about the missing weapons', 'INSERT', 97);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '                3. hit region defined by line of sight', 'INSERT', 98);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '            - **MISSILE**', 'INSERT', 99);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '                1. detect which missiles are in explosion range', 'INSERT', 100);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '                2. fire the target''s **POINT DEFENSE** and **COUNTER MISSILE**', 'INSERT', 101);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '                3. reduce the amount of incoming missiles by the anti-missile-hits', 'INSERT', 102);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '                4. hit region defines by fire control solution', 'INSERT', 103);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    2. detect hit region by fire control solution and target''s last movement and **project damage**', 'INSERT', 104);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 105);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - **repeat until every weapon has processed**', 'INSERT', 106);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 107);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 108);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '4. ### fire weapons', 'INSERT', 109);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 110);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '   ###### loose off weapons by initiative - the better, the later', 'INSERT', 111);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 112);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    1. determine initiative for all combat capable fleets', 'INSERT', 113);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        1. the fleet detects incoming weapon fire, the opponent''s distance and movement to state the situation', 'INSERT', 114);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        2. detect which weapon systems are in alignment to the target', 'INSERT', 115);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '            - detect best fire scenario to suit the mission type', 'INSERT', 116);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '                - combat control system will allocate missile control channels between anti missile combat or offensive weapon systems', 'INSERT', 117);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        3. create fire control solutions for every single weapon to their specific target', 'INSERT', 118);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        4. loose off all weapon systems', 'INSERT', 119);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 120);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '        - **repeat until every fleet has fired**', 'INSERT', 121);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 122);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '### Movement', 'INSERT', 123);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 124);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '###### movement motivations:', 'INSERT', 125);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 126);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '1. escape movement', 'INSERT', 127);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - leaving the combat area', 'INSERT', 128);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - maximal reduction of the bombardment time / weapon phases', 'INSERT', 129);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '2. stay out of weapon range', 'INSERT', 130);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - staying in the combat area', 'INSERT', 131);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - actively moving out of weapon range', 'INSERT', 132);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - maximal reduction of the bombardment time / weapon phases', 'INSERT', 133);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '3. do not harm', 'INSERT', 134);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - do not lower the distance to the opponent', 'INSERT', 135);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '4. maximize damage projection', 'INSERT', 136);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - the fleet will evaluate their own abilities against the opponent and chose the best distance', 'INSERT', 137);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 138);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '###### movement types:', 'INSERT', 139);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 140);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '1. hold distance', 'INSERT', 141);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '2. reduce distance', 'INSERT', 142);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '3. increase distance', 'INSERT', 143);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 144);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '   **all distance moves:**', 'INSERT', 145);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - possibly changes the distance to the opponent', 'INSERT', 146);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - normal ability to fire weapon systems', 'INSERT', 147);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - normal chance of being hit', 'INSERT', 148);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '4. wedge protection', 'INSERT', 149);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - keeps the last course and speed', 'INSERT', 150);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - rolls and yaws the ships to put the sidewall between the incoming weapons and themselves', 'INSERT', 151);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - reduced ability to fire weapon systems nearly to zero', 'INSERT', 152);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - reduced chance of being hit nearly to zero', 'INSERT', 153);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '5. offensive roll', 'INSERT', 154);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - keeps the last course and speed', 'INSERT', 155);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - rolls and yaws the ships to put the most effective weapon systems towards the foe', 'INSERT', 156);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - increases ability to fire weapon systems to maximum', 'INSERT', 157);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - increases chance of being hit to maximum', 'INSERT', 158);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '6. evasion movement', 'INSERT', 159);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - possibly changes the distance to the opponent', 'INSERT', 160);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - reduced ability to fire weapon systems', 'INSERT', 161);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '    - reduced chance of being hit', 'INSERT', 162);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 163);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '### Decision-making', 'INSERT', 164);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 165);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '#### continue or abort a combat', 'INSERT', 166);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 167);
INSERT INTO articleLines (idArticleRevision, content, deltaType, lineNo) VALUES (4, '', 'INSERT', 168);

INSERT INTO article_articleRevisions (Article_idArticle, articleRevisions_idArticleRevision) VALUES (1, 1);
INSERT INTO article_articleRevisions (Article_idArticle, articleRevisions_idArticleRevision) VALUES (2, 2);
INSERT INTO article_articleRevisions (Article_idArticle, articleRevisions_idArticleRevision) VALUES (3, 3);
INSERT INTO article_articleRevisions (Article_idArticle, articleRevisions_idArticleRevision) VALUES (4, 4);

insert into dbPatch values (null, now(), 'add wiki', '0.0.6-2');