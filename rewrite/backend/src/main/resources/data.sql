delete from map_operation_log;
delete from map_cells;
delete from maps;

insert into maps (map_id, revision) values ('demo-map', 0);

insert into map_cells (map_id, q, r, terrain, terrain_hidden, feature_hidden)
values
    ('demo-map', 0, 0, 'plains', false, false),
    ('demo-map', 1, 0, 'forest', false, false),
    ('demo-map', 2, 0, 'hills', false, false);