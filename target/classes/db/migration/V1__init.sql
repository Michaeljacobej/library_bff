create table books (
  id bigserial primary key,
  title varchar(255) not null,
  author varchar(255) not null,
  isbn varchar(64) not null unique,
  total_copies integer not null check (total_copies >= 0),
  available_copies integer not null check (available_copies >= 0),
  check (available_copies <= total_copies)
);

create table members (
  id bigserial primary key,
  name varchar(255) not null,
  email varchar(255) not null unique
);

create table loans (
  id bigserial primary key,
  book_id bigint not null references books(id),
  member_id bigint not null references members(id),
  borrowed_at timestamp with time zone not null,
  due_date timestamp with time zone not null,
  returned_at timestamp with time zone
);

create index idx_loans_member_active on loans (member_id) where returned_at is null;
create index idx_loans_due_date on loans (due_date);
