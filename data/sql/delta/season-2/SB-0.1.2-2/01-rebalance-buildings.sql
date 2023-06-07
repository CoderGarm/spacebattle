-- building resource and construction costs * 1000
update resourcesDepositComposition set amount = amount * 1000  where idResourceDeposit in (select idCosts from building);

-- base value of non-pop, collectable-productive buildings * 50
update building set baseValue = baseValue * 50 where productionCategory = 'PRODUCE' and productionTarget not in ('RESEARCH', 'CONSTRUCTION', 'ORBITAL_CONSTRUCTION', 'POPULATION');

-- shipyards * 30
update building set baseValue = baseValue * 30 where productionCategory = 'PRODUCE' and productionTarget in ('ORBITAL_CONSTRUCTION');

-- ground constructions * 500
update building set baseValue = baseValue * 500 where productionCategory = 'PRODUCE' and productionTarget in ('CONSTRUCTION');

insert into dbPatch values (null, now(), 'rebalance buildings', '0.1.2-2');
