CREATE TABLE items
(
    id           SERIAL PRIMARY KEY NOT NULL,
    title        varchar(255)       NOT NULL,
    release_year int                NOT NULL,
    format       varchar(50)        NOT NULL,
    artists      varchar[]          NOT NULL,
    images       varchar[]          NOT NULL
);

CREATE TABLE item_tracks
(
    item_id       int       NOT NULL,
    position      int       NOT NULL,
    name          varchar   NOT NULL,
    track_artists varchar[] NOT NULL,
    source_url    varchar,
    PRIMARY KEY (item_id, position),
    FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
);

CREATE TABLE artists
(
    id   SERIAL PRIMARY KEY NOT NULL,
    name varchar            NOT NULL,
    UNIQUE (name)
);

CREATE TABLE tags
(
    id       SERIAL PRIMARY KEY NOT NULL,
    category varchar            NOT NULL,
    name     varchar            NOT NULL,
    shade    varchar            NOT NULL,
    UNIQUE (category, name)
);

CREATE TABLE item_tags
(
    item_id int NOT NULL,
    tag_id  int NOT NULL,
    PRIMARY KEY (item_id, tag_id),
    FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);