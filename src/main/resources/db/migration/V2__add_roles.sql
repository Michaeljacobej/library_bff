create table roles (
  id bigserial primary key,
  name varchar(64) not null unique
);

insert into roles (name) values ('ADMIN') on conflict do nothing;
insert into roles (name) values ('LIBRARIAN') on conflict do nothing;
insert into roles (name) values ('MEMBER') on conflict do nothing;

alter table members add column role_id bigint;
update members set role_id = (select id from roles where name = 'MEMBER') where role_id is null;
alter table members add constraint fk_members_role foreign key (role_id) references roles(id);
alter table members alter column role_id set not null;

create index idx_members_role_id on members(role_id);
