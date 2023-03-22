
alter table user add column isLoginForbidden bit not null default false after gameUserRoles;
alter table user add column noEMailWanted bit not null default false after isLoginForbidden;
alter table user add column isEMailVerified bit not null default false after noEMailWanted;

# noinspection SqlWithoutWhere
update user set isEMailVerified = true;
update user set isLoginForbidden = true where username = 'Defeated Opponent';

update user set gameUserRoles = 'FORUM_READ|FORUM_WRITE' where gameUserRoles = '';
update user set gameUserRoles = 'ALLIANCE_ADMIN|FORUM_READ|FORUM_WRITE' where gameUserRoles = 'ALLIANCE_ADMIN';

update user set gameUserRoles = concat(gameUserRoles, '|WIKI_ADMIN') where userRole = 'ADMIN';

insert into dbPatch values (null, now(), 'adding password change and flags', '0.0.12-1');
