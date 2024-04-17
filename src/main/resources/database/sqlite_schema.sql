-- Define the collection_box table
CREATE TABLE IF NOT EXISTS collection_box (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    playerUUID TEXT NOT NULL,
    itemStack TEXT NOT NULL,
    dateAdded INTEGER NOT NULL
);

-- Define the expired_items table
CREATE TABLE IF NOT EXISTS expired_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    playerUUID TEXT NOT NULL,
    itemStack TEXT NOT NULL,
    dateAdded INTEGER NOT NULL
);

-- Define the listings table
CREATE TABLE IF NOT EXISTS listings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid TEXT NOT NULL,
    ownerUUID TEXT NOT NULL,
    ownerName TEXT NOT NULL,
    category TEXT NOT NULL,
    creationDate INTEGER NOT NULL,
    deletionDate INTEGER NOT NULL,
    price REAL NOT NULL,
    itemStack TEXT NOT NULL
);