alter table books add column deleted_at timestamp with time zone;
create index idx_books_deleted_at on books (deleted_at);

alter table members add column deleted_at timestamp with time zone;
create index idx_members_deleted_at on members (deleted_at);

create table reservations (
  id bigserial primary key,
  book_id bigint not null references books(id),
  member_id bigint not null references members(id),
  role_name varchar(64) not null,
  status varchar(16) not null,
  created_at timestamp with time zone not null,
  fulfilled_at timestamp with time zone,
  canceled_at timestamp with time zone
);

create index idx_reservations_book_status on reservations (book_id, status, created_at);
create index idx_reservations_member_status on reservations (member_id, status, created_at);
