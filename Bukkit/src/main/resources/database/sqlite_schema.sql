CREATE TABLE IF NOT EXISTS collection_box
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    playerUUID TEXT    NOT NULL,
    itemStack  TEXT    NOT NULL,
    dateAdded  INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS expired_items
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    playerUUID TEXT    NOT NULL,
    itemStack  TEXT    NOT NULL,
    dateAdded  INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS listings
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid         TEXT    NOT NULL,
    ownerUUID    TEXT    NOT NULL,
    ownerName    TEXT    NOT NULL,
    category     TEXT    NOT NULL,
    creationDate INTEGER NOT NULL,
    deletionDate INTEGER NOT NULL,
    price        REAL    NOT NULL,
    tax          REAL    NOT NULL,
    itemStack    TEXT    NOT NULL,
    biddable     INTEGER NOT NULL,
    bids         TEXT NULLABLE
);

CREATE TABLE IF NOT EXISTS historyV2
(
    playerUUID TEXT NOT NULL PRIMARY KEY,
    items TEXT NOT NULL
);