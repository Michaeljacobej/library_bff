create table if not exists roles (
  id bigserial primary key,
  name varchar(64) not null unique
);

insert into roles (name) values ('ADMIN') on conflict do nothing;
insert into roles (name) values ('LIBRARIAN') on conflict do nothing;
insert into roles (name) values ('MEMBER') on conflict do nothing;

alter table members add column if not exists role_id bigint;
update members set role_id = (select id from roles where name = 'MEMBER') where role_id is null;

do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'fk_members_role'
  ) then
    alter table members add constraint fk_members_role foreign key (role_id) references roles(id);
  end if;
end $$;

alter table members alter column role_id set not null;

create index if not exists idx_members_role_id on members(role_id);
