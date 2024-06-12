package info.preva1l.fadah.data;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.BukkitListing;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.ItemSerializer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SQLiteDatabase implements Database {
    private static final String DATABASE_FILE_NAME = "FadahData.db";
    @Getter @Setter private boolean connected = false;
    private File databaseFile;
    private HikariDataSource hikariDataSource;

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private synchronized String[] getSchemaStatements(@NotNull String schemaFileName) throws IOException {
        return new String(Objects.requireNonNull(Fadah.getINSTANCE().getResource(schemaFileName))
                .readAllBytes(), StandardCharsets.UTF_8).split(";");
    }

    private synchronized Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    private synchronized void setConnection() {
        try {
            if (databaseFile.createNewFile()) {
                Fadah.getConsole().info("Created the SQLite database file");
            }

            Class.forName("org.sqlite.JDBC");

            HikariConfig config = new HikariConfig();
            config.setPoolName("FadahHikariPool");
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            config.setConnectionTestQuery("SELECT 1");
            config.setMaxLifetime(60000);
            config.setIdleTimeout(45000);
            config.setMaximumPoolSize(50);
            hikariDataSource = new HikariDataSource(config);

        } catch (IOException e) {
            Fadah.getConsole().log(Level.SEVERE, "An exception occurred creating the database file", e);
        } catch (ClassNotFoundException e) {
            Fadah.getConsole().log(Level.SEVERE, "Failed to load the necessary SQLite driver", e);
        }
    }

    private synchronized void backupFlatFile(@NotNull File file) {
        if (!file.exists()) {
            return;
        }

        final File backup = new File(file.getParent(), String.format("%s.bak", file.getName()));
        try {
            if (!backup.exists() || backup.delete()) {
                Files.copy(file.toPath(), backup.toPath());
            }
        } catch (IOException e) {
            Fadah.getConsole().log(Level.WARNING, "Failed to backup flat file database", e);
        }
    }

    @Override
    public synchronized CompletableFuture<Void> connect() {
        return CompletableFuture.supplyAsync(() -> {
            databaseFile = new File(Fadah.getINSTANCE().getDataFolder(), DATABASE_FILE_NAME);

            this.setConnection();
            this.backupFlatFile(databaseFile);

            try {
                final String[] databaseSchema = getSchemaStatements(String.format("database/%s_schema.sql", Config.DATABASE_TYPE.toDBTypeEnum().getId()));
                try (Statement statement = getConnection().createStatement()) {
                    for (String tableCreationStatement : databaseSchema) {
                        statement.execute(tableCreationStatement);
                    }
                } catch (SQLException e) {
                    destroy();
                    throw new IllegalStateException("Failed to create database tables. Please ensure you are running MySQL v8.0+ " +
                            "and that your connecting user account has privileges to create tables.", e);
                }
            } catch (IOException e) {
                destroy();
                throw new IllegalStateException("Failed to create database tables. Please ensure you are running MySQL v8.0+ " +
                        "and that your connecting user account has privileges to create tables.", e);
            }
            setConnected(true);
            this.loadListings();
            return null;
        });
    }

    @Override
    public synchronized void destroy() {
        try {
            if (getConnection() != null) {
                if (!getConnection().isClosed()) {
                    getConnection().close();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        setConnected(false);
    }

    @Override
    public synchronized CompletableFuture<Void> addToCollectionBox(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO `collection_box`
                        (`playerUUID`,`itemStack`,`dateAdded`)
                        VALUES (?,?,?);""")) {
                    statement.setString(1, playerUUID.toString());
                    statement.setString(2, ItemSerializer.serialize(collectableItem.itemStack()));
                    statement.setLong(3, collectableItem.dateAdded());
                    statement.execute();
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to add item to collection box!");
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<Void> removeFromCollectionBox(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        DELETE FROM `collection_box`
                        WHERE `playerUUID`=? AND `itemStack`=? AND `dateAdded` =?;""")) {
                    statement.setString(1, playerUUID.toString());
                    statement.setString(2, ItemSerializer.serialize(collectableItem.itemStack()));
                    statement.setLong(3, collectableItem.dateAdded());
                    statement.execute();
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to remove item from collection box!");
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<List<CollectableItem>> getCollectionBox(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(Collections::emptyList);
        }
        return CompletableFuture.supplyAsync(() -> {
            final List<CollectableItem> retrievedData = Lists.newArrayList();
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT `itemStack`,  `dateAdded`
                        FROM `collection_box`
                        WHERE `playerUUID`=?;""")) {
                    statement.setString(1, playerUUID.toString());
                    final ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                        final long dateAdded = resultSet.getLong("dateAdded");
                        retrievedData.add(new CollectableItem(itemStack, dateAdded));
                    }
                    return retrievedData;
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to get items from collection box!");
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public synchronized CompletableFuture<Void> addToExpiredItems(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO `expired_items`
                        (`playerUUID`,`itemStack`,`dateAdded`)
                        VALUES (?,?,?);""")) {
                    statement.setString(1, playerUUID.toString());
                    statement.setString(2, ItemSerializer.serialize(collectableItem.itemStack()));
                    statement.setLong(3, collectableItem.dateAdded());
                    statement.execute();
                }
            } catch (SQLException e) {
                Fadah.getConsole().log(Level.SEVERE, "Failed to add item to expired items!", e);
            }
            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<Void> removeFromExpiredItems(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        DELETE FROM `expired_items`
                        WHERE `playerUUID`=? AND `itemStack`=? AND `dateAdded` =?;""")) {
                    statement.setString(1, playerUUID.toString());
                    statement.setString(2, ItemSerializer.serialize(collectableItem.itemStack()));
                    statement.setLong(3, collectableItem.dateAdded());
                    statement.execute();
                }
            } catch (SQLException e) {
                Fadah.getConsole().log(Level.SEVERE, "Failed to remove item from expired items!", e);
            }
            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<List<CollectableItem>> getExpiredItems(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(Collections::emptyList);
        }
        return CompletableFuture.supplyAsync(() -> {
            final List<CollectableItem> retrievedData = Lists.newArrayList();
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT `itemStack`,  `dateAdded`
                        FROM `expired_items`
                        WHERE `playerUUID`=?;""")) {
                    statement.setString(1, playerUUID.toString());
                    final ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                        final long dateAdded = resultSet.getLong("dateAdded");
                        retrievedData.add(new CollectableItem(itemStack, dateAdded));
                    }
                    return retrievedData;
                }
            } catch (SQLException e) {
                Fadah.getConsole().log(Level.SEVERE, "Failed to get items from expired items!", e);
            }
            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<Void> addListing(Listing listing) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO `listings`
                        (`uuid`,`ownerUUID`,`ownerName`, `category`, `creationDate`, `deletionDate`, `price`, `tax`, `itemStack`)
                        VALUES (?,?,?,?,?,?,?,?,?);""")) {
                    statement.setString(1, listing.getId().toString());
                    statement.setString(2, listing.getOwner().toString());
                    statement.setString(3, listing.getOwnerName());
                    statement.setString(4, listing.getCategoryID());
                    statement.setLong(5, listing.getCreationDate());
                    statement.setLong(6, listing.getDeletionDate());
                    statement.setDouble(7, listing.getPrice());
                    statement.setDouble(8, listing.getTax());
                    statement.setString(9, ItemSerializer.serialize(listing.getItemStack()));
                    statement.execute();
                }
            } catch (SQLException e) {
                Fadah.getConsole().log(Level.SEVERE, "Failed to add item to listings!", e);
            }
            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<Void> removeListing(UUID id) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        DELETE FROM `listings`
                        WHERE uuid = ?;""")) {
                    statement.setString(1, id.toString());
                    statement.execute();
                }
            } catch (SQLException e) {
                Fadah.getConsole().log(Level.SEVERE, "Failed to remove item from listings!", e);
            }
            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<List<Listing>> getListings() {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            final List<Listing> retrievedData = Lists.newArrayList();
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT  `uuid`, `ownerUUID`, `ownerName`, `category`, `creationDate`, `deletionDate`, `price`, `tax`, `itemStack`
                        FROM `listings`;""")) {
                    final ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        final UUID id = UUID.fromString(resultSet.getString("uuid"));
                        final UUID ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"));
                        final String ownerName = resultSet.getString("ownerName");
                        final String categoryID = resultSet.getString("category");
                        final long creationDate = resultSet.getLong("creationDate");
                        final long deletionDate = resultSet.getLong("deletionDate");
                        final double price = resultSet.getDouble("price");
                        final double tax = resultSet.getDouble("tax");
                        final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                        retrievedData.add(new BukkitListing(id, ownerUUID, ownerName, itemStack, categoryID, price, tax, creationDate, deletionDate));
                    }
                    return retrievedData;
                }
            } catch (SQLException e) {
                Fadah.getConsole().log(Level.SEVERE, "Failed to get items from listings!", e);
            }
            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<Listing> getListing(UUID id) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT  `ownerUUID`, `ownerName`, `category`, `creationDate`, `deletionDate`, `price`, `tax`, `itemStack`
                        FROM `listings`
                        WHERE `uuid`=?;""")) {
                    statement.setString(1, id.toString());
                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        final UUID ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"));
                        final String ownerName = resultSet.getString("ownerName");
                        final String categoryID = resultSet.getString("category");
                        final long creationDate = resultSet.getLong("creationDate");
                        final long deletionDate = resultSet.getLong("deletionDate");
                        final double price = resultSet.getDouble("price");
                        final double tax = resultSet.getDouble("tax");
                        final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                        return new BukkitListing(id, ownerUUID, ownerName, itemStack, categoryID, price, tax, creationDate, deletionDate);
                    }
                }
            } catch (SQLException e) {
                Fadah.getConsole().log(Level.SEVERE, "Failed to get listing!", e);
            }
            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<Void> addToHistory(UUID playerUUID, HistoricItem historicItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO `history`
                        (`playerUUID`, `itemStack`, `loggedDate`, `loggedAction`, `price`, `purchaserUUID`)
                        VALUES (?,?,?,?,?,?);""")) {
                    statement.setString(1, playerUUID.toString());
                    statement.setString(2, ItemSerializer.serialize(historicItem.itemStack()));
                    statement.setLong(3, historicItem.loggedDate());
                    statement.setInt(4, historicItem.action().ordinal());
                    if (historicItem.price() != null) {
                        statement.setDouble(5, historicItem.price());
                    } else {
                        statement.setNull(5, Types.DOUBLE);
                    }
                    if (historicItem.purchaserUUID() != null) {
                        statement.setString(6, historicItem.purchaserUUID().toString());
                    } else {
                        statement.setNull(6, Types.VARCHAR);
                    }
                    statement.execute();
                }
            } catch (SQLException e) {
                Fadah.getConsole().log(Level.SEVERE, "Failed to add item to history!", e);
            }
            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<List<HistoricItem>> getHistory(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(Collections::emptyList);
        }
        return CompletableFuture.supplyAsync(() -> {
            final List<HistoricItem> retrievedData = Lists.newArrayList();
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT `itemStack`, `loggedDate`, `loggedAction`, `price`, `purchaserUUID`
                        FROM `history`
                        WHERE `playerUUID`=?;""")) {
                    statement.setString(1, playerUUID.toString());
                    final ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        final long loggedDate = resultSet.getLong("loggedDate");
                        final double price = resultSet.getDouble("price");
                        final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                        final HistoricItem.LoggedAction loggedAction = HistoricItem.LoggedAction.values()[resultSet.getInt("loggedAction")];
                        final UUID purchaserUUID = resultSet.getString("purchaserUUID") == null ? null : UUID.fromString(resultSet.getString("purchaserUUID"));
                        retrievedData.add(new HistoricItem(playerUUID, loggedDate, loggedAction, itemStack, price, purchaserUUID));
                    }
                    return retrievedData;
                }
            } catch (SQLException e) {
                Fadah.getConsole().log(Level.SEVERE, "Failed to get items from history!", e);
            }
            return null;
        });
    }
}
