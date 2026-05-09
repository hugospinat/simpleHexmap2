# 4 representations cibles

Ce document formalise les 4 representations a separer dans une future implementation repartie proprement entre backend, protocole et frontend.

Objectif : eviter qu'un seul objet porte a la fois la persistence SQL, la logique metier, le transport reseau et les besoins du renderer.

Le principe directeur est simple :

- la DB stocke
- le domaine decide
- le transport expose
- le rendu dessine

## Vue d'ensemble

| Representation | Role principal | Optimisee pour | Ne doit pas faire |
|---|---|---|---|
| Persistence model | Stockage durable | SQL, index, contraintes, migrations | Porter la logique metier centrale |
| Domain model | Source de verite metier | Invariants, commandes, coherence | Dependre du schema SQL ou du renderer |
| Transport model | Contrat backend <-> frontend | Versionnement, snapshots, messages | Etre le coeur metier |
| Render model | Projection de rendu frontend | Couches visuelles, ordre de dessin | Devenir une source de verite fonctionnelle |

## 1. Persistence model

Le persistence model est la representation orientee base de donnees.

### Responsabilites

- stocker les donnees durablement
- appliquer les contraintes SQL
- supporter les index et les requetes
- permettre des migrations lisibles et auditables
- garantir l'integrite relationnelle

### Principes

- structure normalisee ou semi-normalisee selon les besoins de requete
- clefs primaires et etrangeres explicites
- colonnes et tables adaptees aux transactions reelles
- aucune logique de rendu
- aucune logique UI
- le moins possible de logique metier complexe

### Schema cible complet et normalise

La proposition ci-dessous vise une base complete, normalisee et exploitable pour une implementation backend autoritaire.

Choix retenu dans cette version :

- cle surrogate pour les entites spatiales et techniques de persistence
- contraintes `unique` pour conserver l'unicite metier utile
- FKs simples basees autant que possible sur des identifiants techniques
- suppression des colonnes derivees quand elles peuvent etre retrouvees via une FK parent

Organisation par domaine :

- authentification
- collaboration workspace
- cartes et contenu cartographique
- historique d'operations

### Authentification

`users`

- `id` PK UUID
- `username` VARCHAR not null
- `username_normalized` VARCHAR not null unique
- `password_hash` VARCHAR not null
- `created_at` TIMESTAMPTZ not null
- `updated_at` TIMESTAMPTZ not null

`sessions`

- `id` PK UUID
- `user_id` FK -> `users.id` not null
- `token_hash` VARCHAR not null unique
- `created_at` TIMESTAMPTZ not null
- `expires_at` TIMESTAMPTZ not null
- `last_seen_at` TIMESTAMPTZ not null
- `revoked_at` TIMESTAMPTZ null

Contraintes :

- index sur `sessions.user_id`
- index sur `sessions.expires_at`

### Collaboration workspace

`workspaces`

- `id` PK UUID
- `name` VARCHAR not null
- `created_at` TIMESTAMPTZ not null
- `updated_at` TIMESTAMPTZ not null

`workspace_members`

- `workspace_id` FK -> `workspaces.id` not null
- `user_id` FK -> `users.id` not null
- `role` VARCHAR not null
- `token_color` VARCHAR not null
- `created_at` TIMESTAMPTZ not null
- `updated_at` TIMESTAMPTZ not null
- PK (`workspace_id`, `user_id`)

Contraintes :

- `role in ('owner', 'gm', 'player')`
- index sur `workspace_members.user_id`

`workspace_invites`

- `id` PK UUID
- `workspace_id` FK -> `workspaces.id` not null
- `created_by_user_id` FK -> `users.id` not null
- `role` VARCHAR not null
- `token_hash` VARCHAR not null unique
- `expires_at` TIMESTAMPTZ not null
- `max_uses` INTEGER not null
- `used_count` INTEGER not null default 0
- `revoked_at` TIMESTAMPTZ null
- `created_at` TIMESTAMPTZ not null
- `updated_at` TIMESTAMPTZ not null

Contraintes :

- `role in ('player')` tant que les invites GM ne sont pas un besoin produit
- `max_uses > 0`
- `used_count >= 0 and used_count <= max_uses`
- index sur `workspace_invites.workspace_id`

### Cartes

`maps`

- `id` PK UUID
- `workspace_id` FK -> `workspaces.id` not null
- `name` VARCHAR not null
- `revision` BIGINT not null
- `created_at` TIMESTAMPTZ not null
- `updated_at` TIMESTAMPTZ not null

Contraintes :

- index sur `maps.workspace_id`
- `revision >= 0`

### Referentiels de carte

`map_factions`

- `id` PK UUID
- `map_id` FK -> `maps.id` not null
- `name` VARCHAR not null
- `color` VARCHAR not null
- `created_at` TIMESTAMPTZ not null
- `updated_at` TIMESTAMPTZ not null

Contraintes :

- `color` doit respecter le format de couleur choisi par le domaine
- index sur `map_factions.map_id`
- unique (`map_id`, `name`)

### Cellules

`map_cells`

- `id` PK UUID
- `map_id` FK -> `maps.id` not null
- `q` INTEGER not null
- `r` INTEGER not null
- `terrain_type` VARCHAR not null
- `created_at` TIMESTAMPTZ not null
- `updated_at` TIMESTAMPTZ not null

Contraintes :

- unique (`map_id`, `q`, `r`)
- index sur (`map_id`, `q`, `r`)
- index sur (`map_id`, `terrain_type`)

`map_cell_visibility`

- `cell_id` PK UUID
- `terrain_hidden` BOOLEAN not null
- `feature_hidden` BOOLEAN not null
- `updated_at` TIMESTAMPTZ not null
- FK (`cell_id`) -> `map_cells(id)`

Pourquoi une table separee :

- la visibilite est un axe fonctionnel distinct du terrain
- les evolutions futures sur la fog of war restent localisees
- on evite d'entasser des responsabilites differentes dans `map_cells`

`map_cell_notes`

- `cell_id` PK UUID
- `gm_title` TEXT null
- `player_title` TEXT null
- `gm_markdown` TEXT null
- `updated_at` TIMESTAMPTZ not null
- FK (`cell_id`) -> `map_cells(id)`

Pourquoi une table separee :

- une note est optionnelle
- la note a son propre cycle de vie
- la note ne doit pas alourdir la ligne de cellule si elle est absente

`map_cell_territories`

- `cell_id` PK UUID
- `faction_id` UUID not null
- `updated_at` TIMESTAMPTZ not null
- FK (`cell_id`) -> `map_cells(id)`
- FK (`faction_id`) -> `map_factions(id)`

Pourquoi une table separee :

- l'appartenance territoriale est une projection politique, pas une propriete intrinsinseque du terrain
- on garde le schema evolutif si les territoires deviennent plus riches

### Aretes

`map_edges`

- `cell_a_id` UUID not null
- `cell_b_id` UUID not null
- `created_at` TIMESTAMPTZ not null
- `updated_at` TIMESTAMPTZ not null
- PK (`cell_a_id`, `cell_b_id`)
- FK (`cell_a_id`) -> `map_cells(id)`
- FK (`cell_b_id`) -> `map_cells(id)`

Contraintes :

- `cell_a_id <> cell_b_id`
- la paire doit etre canonique, par exemple `cell_a_id < cell_b_id`

Pourquoi cette representation :

- une arete n'existe qu'une seule fois, au lieu d'etre doublee par orientation
- les deux cellules de l'arete sont explicites
- le cascade delete est force naturellement via les deux FKs vers `map_cells`
- la cle primaire exprime directement l'identite relationnelle de l'arete

Note de coherence :

- la contrainte "les deux cellules sont voisines"
- et la contrainte "les deux cellules appartiennent a la meme map"

doivent etre garanties par le domaine ou par une contrainte SQL avancee de type trigger `constraint`

`map_edge_roads`

- `cell_a_id` UUID not null
- `cell_b_id` UUID not null
- `updated_at` TIMESTAMPTZ not null
- PK (`cell_a_id`, `cell_b_id`)
- FK (`cell_a_id`, `cell_b_id`) -> `map_edges(cell_a_id, cell_b_id)`

`map_edge_rivers`

- `cell_a_id` UUID not null
- `cell_b_id` UUID not null
- `updated_at` TIMESTAMPTZ not null
- PK (`cell_a_id`, `cell_b_id`)
- FK (`cell_a_id`, `cell_b_id`) -> `map_edges(cell_a_id, cell_b_id)`

Pourquoi separer routes et rivieres :

- ce sont deux couches fonctionnelles distinctes
- chacune peut evoluer sans impacter l'autre
- les contraintes et metadonnees futures restent ouvertes

### Features

`map_features`

- `id` PK UUID
- `kind` VARCHAR not null
- `feature_level` SMALLINT not null
- `anchor_cell_id` UUID not null
- `hidden` BOOLEAN not null
- `created_at` TIMESTAMPTZ not null
- `updated_at` TIMESTAMPTZ not null
- FK (`anchor_cell_id`) -> `map_cells(id)`

Contraintes :

- `feature_level in (1, 2, 3)`

Index recommandes :

- index sur `map_features.anchor_cell_id`
- index sur `map_features.kind`

### Tokens

`map_tokens`

- `id` PK UUID
- `map_id` UUID not null
- `user_id` FK -> `users.id` not null
- `created_at` TIMESTAMPTZ not null
- `updated_at` TIMESTAMPTZ not null
- FK (`map_id`) -> `maps.id`

Pourquoi cette cle :

- un utilisateur n'a qu'un token place par carte
- l'entite token represente l'existence du token dans une carte, independamment de sa position courante

Contraintes :

- unique (`map_id`, `user_id`)

`map_token_positions`

- `token_id` PK UUID
- `cell_id` UUID not null
- `updated_at` TIMESTAMPTZ not null
- FK (`token_id`) -> `map_tokens(id)`
- FK (`cell_id`) -> `map_cells(id)`

Pourquoi separer identite et position :

- `map_tokens` porte l'identite metier "un token pour un utilisateur sur une carte"
- `map_token_positions` porte le placement courant
- on evite de dupliquer `map_id` dans la ligne qui reference une cellule

Note de normalisation :

- la coherence "la cellule de placement appartient a la meme map que le token" doit etre garantie par le domaine ou par une contrainte SQL avancee

### Historique d'operations

`map_operation_log`

- `map_id` FK -> `maps.id` not null
- `sequence` BIGINT not null
- `operation_id` VARCHAR not null
- `source_client_id` VARCHAR not null
- `actor_user_id` FK -> `users.id` not null
- `operation_type` VARCHAR not null
- `operation_payload` JSONB not null
- `created_at` TIMESTAMPTZ not null
- PK (`map_id`, `sequence`)

Contraintes :

- unique (`map_id`, `operation_id`)
- index sur (`map_id`, `created_at`)
- index sur (`map_id`, `operation_type`)

Pourquoi garder un payload JSONB ici :

- le log d'operations est un journal applicatif, pas un modele relationnel de lecture
- l'idempotence et le replay sont plus simples a conserver
- le write model reste separe des projections relationnelles

### Resume des dependances

- `users` -> `sessions`
- `workspaces` -> `workspace_members`, `workspace_invites`, `maps`
- `maps` -> `map_cells`, `map_factions`, `map_operation_log`
- `maps` -> `map_tokens`
- `map_cells` -> `map_cell_visibility`, `map_cell_notes`, `map_cell_territories`, `map_edges`, `map_features`, `map_token_positions`
- `map_factions` -> `map_cell_territories`
- `map_edges` -> `map_edge_roads`, `map_edge_rivers`
- `map_tokens` -> `map_token_positions`

### Ce que cette normalisation apporte

- les concepts optionnels sont separes du noyau de cellule
- les relations entre carte, cellule, arete, faction et token sont explicites
- les tables filles referencent des identifiants techniques stables plutot que des coordonnees dupliquees
- les colonnes derivees comme `map_id` sont retirees des tables ou elles sont seulement redondantes
- les aretes ne sont plus representees deux fois sous forme orientee
- les contraintes metier simples peuvent etre soutenues par le schema
- la persistence reste evolutive sans imposer la forme du domaine ou du transport

### Limite assumee de cette normalisation stricte

Quand on retire toutes les colonnes derivees, certaines contraintes du type "les deux references appartiennent a la meme map" ne s'expriment plus avec une simple FK standard.

Dans cette version, ces contraintes peuvent etre prises en charge de deux manieres :

- par le domain model et les services transactionnels backend
- par des contraintes SQL avancees, par exemple des triggers `constraint` deferres

### Ce que ce modele n'est pas

- ce n'est pas un DTO reseau
- ce n'est pas l'objet manipule par le renderer
- ce n'est pas la source de verite conceptuelle du jeu d'edition

Le persistence model doit etre la source de verite stockee, pas la source de verite conceptuelle.

## 2. Domain model

Le domain model est la representation metier centrale. C'est la couche la plus importante.

### Responsabilites

- exprimer les invariants
- definir les operations metier
- porter les regles d'edition
- porter les regles de visibilite et de coherence
- valider les commandes utilisateur
- produire un etat coherent apres chaque mutation

### Principes

- modele oriente concepts metier naturels
- independant du SQL
- independant du protocole HTTP / WebSocket
- independant de Pixi ou d'un autre renderer
- structuree autour des vraies unites de la carte

### Objets metier cibles

- `MapAggregate`
- `CellState`
- `EdgeState`
- `Feature`
- `Faction`
- `TokenPlacement`
- `CellNote`

### Exemple de structure cible

```ts
type Coord = { q: number; r: number };

type CellState = {
	coord: Coord;
	terrain: TerrainType;
	visibility: {
		terrainHidden: boolean;
		featureHidden: boolean;
	};
	territoryFactionId: FactionId | null;
	note: CellNote | null;
};

type EdgeState = {
	coord: Coord;
	edge: 0 | 1 | 2 | 3 | 4 | 5;
	road: boolean;
	river: boolean;
};

type MapAggregate = {
	id: string;
	workspaceId: string;
	name: string;
	revision: number;
	cells: Map<string, CellState>;
	edges: Map<string, EdgeState>;
	features: Map<string, Feature>;
	factions: Map<string, Faction>;
	tokens: Map<string, TokenPlacement>;
};
```

### Exemples d'invariants

- une note ne peut exister que sur une cellule existante
- un territoire ne peut referencer qu'une faction existante
- une feature doit etre posee sur une cellule valide
- un token doit respecter les regles de placement et de visibilite
- une arete doit toujours avoir un index compris entre 0 et 5

### Exemples de commandes metier

- `SetCellTerrain`
- `SetCellVisibility`
- `SetCellNote`
- `SetCellTerritory`
- `SetEdgeRoad`
- `SetEdgeRiver`
- `AddFeature`
- `RemoveFeature`
- `UpdateFaction`
- `PlaceToken`

Le domain model recoit des commandes metier, applique les regles, puis produit un nouvel etat coherent.

## 3. Transport model

Le transport model est la representation utilisee pour les echanges entre backend et frontend. C'est le contrat reseau.

### Responsabilites

- exposer un snapshot lisible et stable
- porter les commandes et evenements reseau
- permettre le versionnement du protocole
- filtrer les donnees selon le role GM / player
- decoupler le frontend de la structure interne du domaine

### Principes

- DTO explicites
- formes stables et versionnables
- aucune logique de persistence
- pas d'objets techniques lies au renderer
- pas de fuite du schema interne du domaine

### DTO cibles

- `MapSnapshotDto`
- `PlayerMapSnapshotDto`
- `GmMapSnapshotDto`
- `ApplyMapCommandDto`
- `ApplyTokenCommandDto`
- `MapDeltaDto`
- `MapCommandResultDto`

### Deux familles de messages a distinguer

Messages de commande :

- ce que le client demande au backend
- intentions metier
- validation d'entree stricte

Messages de projection :

- ce que le backend renvoie
- snapshots complets ou deltas autoritaires
- payloads filtres selon le role

### Exemple de structure cible

```ts
type MapSnapshotDto = {
	mapId: string;
	revision: number;
	cells: CellDto[];
	edges: EdgeDto[];
	features: FeatureDto[];
	factions: FactionDto[];
	tokens: TokenDto[];
};
```

Le transport model n'est pas le coeur metier. C'est une projection contractuelle adaptee au reseau.

## 4. Render model

Le render model est la representation adaptee au pipeline de rendu du frontend.

### Responsabilites

- organiser les donnees dans l'ordre utile au rendu
- fournir des structures directement exploitables par les couches du renderer
- reduire le travail de transformation au moment du dessin
- permettre un rendu incremental plus simple si necessaire

### Principes

- modele par couches de rendu
- pas de regles metier deduites dans le renderer
- pas d'objets SQL
- pas de contrat reseau expose tel quel au moteur graphique
- structures adaptees au dessin, pas a l'edition metier

Si le renderer dessine par couches, le render model doit lui aussi etre pense par couches.

### Couches candidates

- `TerrainLayer`
- `FogLayer`
- `RiverLayer`
- `RoadLayer`
- `FactionLayer`
- `FeatureLayer`
- `NoteLayer`
- `TokenLayer`
- `OverlayLayer`

### Exemple de projection cible

```ts
type RenderModel = {
	terrain: TerrainLayer;
	fog: FogLayer;
	rivers: RiverLayer;
	roads: RoadLayer;
	factions: FactionLayer;
	features: FeatureLayer;
	notes: NoteLayer;
	tokens: TokenLayer;
	overlay: OverlayLayer;
};
```

### Ce que ce modele repond

- qu'est-ce qu'il faut dessiner
- dans quel ordre il faut le dessiner
- quelles couches doivent etre recalculees

### Ce qu'il ne doit pas faire

- recalculer des regles metier
- faire autorite sur l'etat reel de la carte
- exposer la structure SQL ou reseau au moteur graphique

## Transformations attendues

Lecture :

`Persistence model -> Domain model -> Transport model -> Render model`

Ecriture :

`User action -> Transport command -> Domain command -> Domain model -> Persistence writes`

Projection frontend :

`Transport snapshot -> Render model par couches -> dessin`

## Regles de separation

- Le persistence model ne doit pas contenir de logique de rendu.
- Le domain model ne doit pas dependre du schema SQL.
- Le transport model ne doit pas imposer la structure interne du domaine.
- Le render model ne doit pas devenir la source de verite metier.
- Une meme structure ne doit pas servir simultanement de schema SQL, DTO reseau et etat de rendu.

## Resultat architectural recherche

La cible n'est pas d'ajouter des couches artificielles. La cible est d'avoir une responsabilite nette pour chaque representation :

- la persistence sait stocker
- le domaine sait decider
- le transport sait contracter
- le rendu sait afficher

## Lecture du code actuel

Le code actuel contient deja des fragments de cette separation, mais de facon incomplete :

- la persistence est cote serveur
- le transport est largement centre sur `MapDocument`
- le domaine frontend est partiel via `MapState`
- le rendu Pixi est deja structure par couches

Le probleme actuel n'est donc pas l'absence totale de separation, mais le fait que `MapDocument` est trop central et que le modele metier n'est pas complet.