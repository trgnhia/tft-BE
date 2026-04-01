create table if not exists "set" (
                                     id bigserial primary key,
                                     is_active boolean not null default true,
                                     name varchar(255) not null,
    created_by bigint,
    updated_at timestamp,
    created_at timestamp not null default current_timestamp
    );

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_set_created_by'
    ) then
alter table "set"
    add constraint fk_set_created_by
        foreign key (created_by) references users(id);
end if;
end $$;

create table if not exists traits (
                                      id bigserial primary key,
                                      set_id bigint not null,
                                      slug varchar(255) not null unique,
    type varchar(100) not null,
    description text,
    breakpoint jsonb,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,
    created_by bigint
    );

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_traits_set'
    ) then
alter table traits
    add constraint fk_traits_set
        foreign key (set_id) references "set"(id) on delete cascade;
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_traits_created_by'
    ) then
alter table traits
    add constraint fk_traits_created_by
        foreign key (created_by) references users(id);
end if;
end $$;

create table if not exists champs (
                                      id bigserial primary key,
                                      set_id bigint not null,
                                      slug varchar(255) not null unique,
    name varchar(255) not null,
    image_url varchar(500),
    stats jsonb,
    created_at timestamp not null default current_timestamp,
    created_by bigint,
    updated_at timestamp
    );

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_champs_set'
    ) then
alter table champs
    add constraint fk_champs_set
        foreign key (set_id) references "set"(id) on delete cascade;
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_champs_created_by'
    ) then
alter table champs
    add constraint fk_champs_created_by
        foreign key (created_by) references users(id);
end if;
end $$;

create table if not exists champ_traits (
                                            id bigserial primary key,
                                            champion_id bigint not null,
                                            trait_id bigint not null,
                                            constraint uk_champ_traits unique (champion_id, trait_id)
    );

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_champ_traits_champion'
    ) then
alter table champ_traits
    add constraint fk_champ_traits_champion
        foreign key (champion_id) references champs(id) on delete cascade;
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_champ_traits_trait'
    ) then
alter table champ_traits
    add constraint fk_champ_traits_trait
        foreign key (trait_id) references traits(id) on delete cascade;
end if;
end $$;

create table if not exists items (
                                     id bigserial primary key,
                                     set_id bigint not null,
                                     stats jsonb,
                                     image_url varchar(500),
    item_component_1 bigint,
    item_component_2 bigint,
    effects jsonb,
    description text,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,
    created_by bigint
    );

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_items_set'
    ) then
alter table items
    add constraint fk_items_set
        foreign key (set_id) references "set"(id) on delete cascade;
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_items_created_by'
    ) then
alter table items
    add constraint fk_items_created_by
        foreign key (created_by) references users(id);
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_items_component_1'
    ) then
alter table items
    add constraint fk_items_component_1
        foreign key (item_component_1) references items(id) on delete set null;
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_items_component_2'
    ) then
alter table items
    add constraint fk_items_component_2
        foreign key (item_component_2) references items(id) on delete set null;
end if;
end $$;

create table if not exists item_recommend (
                                              id bigserial primary key,
                                              champion_id bigint not null,
                                              trait_id bigint not null,
                                              note varchar(255),
    constraint uk_item_recommend unique (champion_id, trait_id)
    );

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_item_recommend_champion'
    ) then
alter table item_recommend
    add constraint fk_item_recommend_champion
        foreign key (champion_id) references champs(id) on delete cascade;
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_item_recommend_trait'
    ) then
alter table item_recommend
    add constraint fk_item_recommend_trait
        foreign key (trait_id) references traits(id) on delete cascade;
end if;
end $$;

create table if not exists team_comp (
                                         id bigserial primary key,
                                         set_id bigint not null,
                                         slug varchar(255) not null unique,
    style varchar(100),
    name varchar(255) not null
    );

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_team_comp_set'
    ) then
alter table team_comp
    add constraint fk_team_comp_set
        foreign key (set_id) references "set"(id) on delete cascade;
end if;
end $$;

create table if not exists team_comp_champ (
                                               team_comp_id bigint not null,
                                               champion_id bigint not null,
                                               constraint pk_team_comp_champ primary key (team_comp_id, champion_id)
    );

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_team_comp_champ_team_comp'
    ) then
alter table team_comp_champ
    add constraint fk_team_comp_champ_team_comp
        foreign key (team_comp_id) references team_comp(id) on delete cascade;
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_team_comp_champ_champion'
    ) then
alter table team_comp_champ
    add constraint fk_team_comp_champ_champion
        foreign key (champion_id) references champs(id) on delete cascade;
end if;
end $$;

create index if not exists idx_traits_set_id on traits(set_id);
create index if not exists idx_champs_set_id on champs(set_id);
create index if not exists idx_items_set_id on items(set_id);
create index if not exists idx_team_comp_set_id on team_comp(set_id);
create index if not exists idx_champ_traits_champion_id on champ_traits(champion_id);
create index if not exists idx_champ_traits_trait_id on champ_traits(trait_id);
create index if not exists idx_item_recommend_champion_id on item_recommend(champion_id);
create index if not exists idx_item_recommend_trait_id on item_recommend(trait_id);
create index if not exists idx_items_component_1 on items(item_component_1);
create index if not exists idx_items_component_2 on items(item_component_2);