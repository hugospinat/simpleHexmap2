create table maps (
    map_id varchar(100) not null,
    revision bigint not null,
    constraint pk_maps primary key (map_id)
);

create table map_cells (
    map_id varchar(100) not null,
    q integer not null,
    r integer not null,
    terrain varchar(32) not null,
    terrain_hidden boolean not null,
    feature_hidden boolean not null,
    territory_faction_id varchar(64),
    constraint pk_map_cells primary key (map_id, q, r),
    constraint fk_map_cells_map foreign key (map_id) references maps (map_id)
);

create table map_operation_log (
    map_id varchar(100) not null,
    operation_id varchar(100) not null,
    sequence bigint not null,
    command_type varchar(64) not null,
    actor_role varchar(32) not null,
    cell_q integer not null,
    cell_r integer not null,
    terrain varchar(32),
    terrain_hidden_value boolean,
    feature_hidden_value boolean,
    territory_faction_id_value varchar(64),
    constraint pk_map_operation_log primary key (map_id, operation_id),
    constraint uq_map_sequence unique (map_id, sequence),
    constraint fk_map_operation_log_map foreign key (map_id) references maps (map_id)
);
