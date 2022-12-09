
alter table warshipHealthStateSnapshot add column isDeleted bit not null default false after idWarshipHealthStateSnapshot;
alter table warshipHealthStateSnapshot add column isOperational bit not null default false after isDeleted;

update warshipHealthStateSnapshot s set isDeleted = true where s.idWarshipHealthStateSnapshot in (
select s.idWarshipHealthStateSnapshot from warshipHealthStateSnapshot s
    left join lossesByHit l on (l.idWarship = s.idWarship)
    left join shipKillerHits h on (h.idBattleReport = s.idBattleReport)
    where h.idShipKillerHit = l.idShipKillerHit
);

update warshipHealthStateSnapshot set isOperational = true where isDeleted = false;

create temporary table tmp_caps as (
select distinct cap.idWarshipHealthState, value, effectValue from warshipCapabilities cap
    left join warshipHealthState state on (state.idWarshipHealthState = cap.idWarshipHealthState)
    left join warShip w on (w.idWarShip = state.idWarship)
    left join shipClass s on (s.idShipClass = w.idShipClass)
    left join armor a on (a.idArmor = s.idArmor)
    where moduleType = 'ARMOR'
        and not exists (select * from hitLog where idTarget = state.idWarship)
        and value < a.effectValue
        and cap.idWarshipHealthState in (
        select idWarshipHealthState from warshipHealthState where idWarship in (
        select idWarShip from warShip where idShipClass in (
        select s.idShipClass from shipClass s where s.idShipClass not in (
        select sf.idShipClass from supportFitting sf natural join passiveModule pm where pm.supportType = 'ARMOR'))))
);

select * from tmp_caps;

update warshipCapabilities cap
    set value = (select t.effectValue from tmp_caps t where cap.idWarshipHealthState = t.idWarshipHealthState)
    where moduleType = 'ARMOR'
    and exists (select t.effectValue from tmp_caps t where cap.idWarshipHealthState = t.idWarshipHealthState);

drop table tmp_caps;

insert into dbPatch values (null, now(), 'add operational to health state snap', '0.0.7-2');