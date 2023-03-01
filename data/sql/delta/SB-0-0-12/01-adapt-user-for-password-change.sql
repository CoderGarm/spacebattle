
alter table user add column isLoginForbidden bit not null default false after gameUserRoles;
alter table user add column noEMailWanted bit not null default false after isLoginForbidden;
alter table user add column isEMailVerified bit not null default false after noEMailWanted;

insert into dbPatch values (null, now(), 'adding password change and flags', '0.0.12-1');
