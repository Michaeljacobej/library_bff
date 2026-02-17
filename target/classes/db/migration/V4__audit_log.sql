create table audit_log (
  id bigserial primary key,
  user_name varchar(255),
  sql_text text,
  executed_at timestamp with time zone not null,
  rows_affected integer,
  success boolean not null,
  error_message text
);

create index idx_audit_log_user_time on audit_log (user_name, executed_at);
