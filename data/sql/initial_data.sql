use sbdb;

insert into user
values (null, "karsten", "passwort", "KANDORIAN", null),
       (null, "steffen", "passwort", "HUMAN", null);
insert into alliance
values (null, "A", "Argonauten"),
       (null, "111er", "111er");

/*
"MININGFACTORS"
"DEPOSITS"
"COSTS"
 */
insert into resourceDeposit
values (null, "DEPOSITS"),
       (null, "MININGFACTORS"),
       (null, "COSTS")
;
/*
"CONSTRUCTION"
"CREDITS"
"METALORE"
"MERCURIUM"
"HYPERONIUM"
 */
insert into resouces
values (null, 0, "CONSTRUCTION"),
       (null, 0, "CREDITS"),
       (null, 0, "METALORE"),
       (null, 0, "MERCURIUM"),
       (null, 0, "HYPERONIUM"),
       (null, 0, "CONSTRUCTION"),
       (null, 0, "CREDITS"),
       (null, 0, "METALORE"),
       (null, 0, "MERCURIUM"),
       (null, 0, "HYPERONIUM"),
;

insert into building
values (null, 10, "CONSTRUCTION", "This is buildung #1", 1.2, "Construction Yard", "CONSTRUCTION")
;