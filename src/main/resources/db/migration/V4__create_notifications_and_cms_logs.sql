create table if not exists notifications (
                                             id bigserial primary key,
                                             message varchar(255) not null,
    title varchar(255),
    content varchar(500),
    target_type varchar(100),
    target_id bigint,
    created_by bigint not null,
    created_at timestamp not null default current_timestamp
    );

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_notifications_created_by'
    ) then
alter table notifications
    add constraint fk_notifications_created_by
        foreign key (created_by) references users(id);
end if;
end $$;

create table if not exists cms_logs (
                                        id bigserial primary key,
                                        username varchar(100),
    endpoint varchar(255) not null,
    http_method varchar(20) not null,
    action_name varchar(100),
    ip_address varchar(45),
    request_body text,
    result_status int not null,
    error_message text,
    start_time timestamp,
    end_time timestamp,
    duration_ms int
    );

create index if not exists idx_notifications_created_by on notifications(created_by);
create index if not exists idx_notifications_target_type_target_id on notifications(target_type, target_id);
create index if not exists idx_cms_logs_username on cms_logs(username);
create index if not exists idx_cms_logs_endpoint on cms_logs(endpoint);
create index if not exists idx_cms_logs_start_time on cms_logs(start_time);