create table if not exists maps (
    map_id varchar(100) primary key,
    revision bigint not null
);

create table if not exists map_cells (
    map_id varchar(100) not null,
    q integer not null,
    r integer not null,
    terrain varchar(32) not null,
    terrain_hidden boolean not null,
    feature_hidden boolean not null,
    primary key (map_id, q, r),
    constraint fk_map_cells_map foreign key (map_id) references maps (map_id) on delete cascade
);

create table if not exists map_operation_log (
    map_id varchar(100) not null,
    operation_id varchar(100) not null,
    sequence bigint not null,
    command_type varchar(64) not null,
    actor_role varchar(32) not null,
    cell_q integer not null,
    cell_r integer not null,
    terrain varchar(32),
    terrain_hidden_value boolean,
    primary key (map_id, operation_id),
    constraint fk_operation_log_map foreign key (map_id) references maps (map_id) on delete cascade,
    constraint uq_map_sequence unique (map_id, sequence)
);