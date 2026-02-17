alter table members
  add column if not exists password_hash varchar(255);
