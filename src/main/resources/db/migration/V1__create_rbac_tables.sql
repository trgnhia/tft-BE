create table roles (
                       id bigserial primary key,
                       code varchar(50) not null unique,
                       name varchar(100) not null,
                       description text,
                       created_at timestamp not null default current_timestamp,
                       updated_at timestamp not null default current_timestamp
);

create table permissions (
                             id bigserial primary key,
                             code varchar(100) not null unique,
                             name varchar(100) not null,
                             description text,
                             created_at timestamp not null default current_timestamp,
                             updated_at timestamp not null default current_timestamp
);

create table users (
                       id bigserial primary key,
                       username varchar(50) not null unique,
                       email varchar(100) not null unique,
                       password_hash varchar(255) not null,
                       enabled boolean not null default true,
                       role_id bigint not null,
                       created_at timestamp not null default current_timestamp,
                       updated_at timestamp not null default current_timestamp,
                       constraint fk_users_role
                           foreign key (role_id) references roles(id)
);

create table role_permissions (
                                  role_id bigint not null,
                                  permission_id bigint not null,
                                  created_at timestamp not null default current_timestamp,
                                  constraint pk_role_permissions primary key (role_id, permission_id),
                                  constraint fk_role_permissions_role
                                      foreign key (role_id) references roles(id) on delete cascade,
                                  constraint fk_role_permissions_permission
                                      foreign key (permission_id) references permissions(id) on delete cascade
);

create index idx_users_role_id on users(role_id);
create index idx_role_permissions_permission_id on role_permissions(permission_id);