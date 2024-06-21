package info.preva1l.fadah.data;

import com.google.common.collect.Lists;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@Setter
@Getter
public class MySQLDatabase implements Database {
    private final String driverClass;
    private boolean connected = false;
    private HikariDataSource dataSource;

    public MySQLDatabase() {
        super();

        this.driverClass = Config.DATABASE_TYPE.toDBTypeEnum() == DatabaseType.MARIADB ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver";
    }

    // stolen from husktowns, thanks bbg will
    @SuppressWarnings("SameParameterValue")
    @NotNull
    private String[] getSchemaStatements(@NotNull String schemaFileName) throws IOException {
        return new String(Objects.requireNonNull(Fadah.getINSTANCE().getResource(schemaFileName))
                .readAllBytes(), StandardCharsets.UTF_8).split(";");
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public CompletableFuture<Void> connect() {
        return CompletableFuture.supplyAsync(() -> {
            dataSource = new HikariDataSource();
            dataSource.setDriverClassName(driverClass);
            dataSource.setJdbcUrl(Config.DATABASE_URI.toString());

            dataSource.setMaximumPoolSize(10);
            dataSource.setMinimumIdle(10);
            dataSource.setMaxLifetime(1800000);
            dataSource.setKeepaliveTime(0);
            dataSource.setConnectionTimeout(5000);
            dataSource.setPoolName("FadahHikariPool");

            final Properties properties = new Properties();
            properties.putAll(
                    Map.of("cachePrepStmts", "true",
                            "prepStmtCacheSize", "250",
                            "prepStmtCacheSqlLimit", "2048",
                            "useServerPrepStmts", "true",
                            "useLocalSessionState", "true",
                            "useLocalTransactionState", "true"
                    ));
            properties.putAll(
                    Map.of(
                            "rewriteBatchedStatements", "true",
                            "cacheResultSetMetadata", "true",
                            "cacheServerConfiguration", "true",
                            "elideSetAutoCommits", "true",
                            "maintainTimeStats", "false")
            );
            dataSource.setDataSourceProperties(properties);

            try (Connection connection = dataSource.getConnection()) {
                final String[] databaseSchema = getSchemaStatements(String.format("database/%s_schema.sql", Config.DATABASE_TYPE.toDBTypeEnum().getId()));
                try (Statement statement = connection.createStatement()) {
                    for (String tableCreationStatement : databaseSchema) {
                        statement.execute(tableCreationStatement);
                    }
                    setConnected(true);
                } catch (SQLException e) {
                    destroy();
                    throw new IllegalStateException("Failed to create database tables. Please ensure you are running MySQL v8.0+ " +
                            "and that your connecting user account has privileges to create tables.", e);
                }
            } catch (SQLException | IOException e) {
                destroy();
                throw new IllegalStateException("Failed to establish a connection to the MySQL database. " +
                        "Please check the supplied database credentials in the config file", e);
            }

            this.loadListings();
            return null;
        });
    }

    @Override
    public void destroy() {
        if (dataSource == null) return;
        if (dataSource.isClosed()) return;
        dataSource.close();
        setConnected(false);
    }

    @Override
    public CompletableFuture<Void> addToCollectionBox(UUID playerUUID, CollectableItem collectableItem) {
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
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to add item to collection box!");
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> removeFromCollectionBox(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        DELETE FROM `collection_box`
                        WHERE `playerUUID`=? AND `itemStack`=? AND `dateAdded` =?
                        LIMIT 1;"""
                )) {
                    statement.setString(1, playerUUID.toString());
                    statement.setString(2, ItemSerializer.serialize(collectableItem.itemStack()));
                    statement.setLong(3, collectableItem.dateAdded());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to remove item from collection box!");
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<CollectableItem>> getCollectionBox(UUID playerUUID) {
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
            }
            return retrievedData;
        });
    }

    @Override
    public CompletableFuture<Void> addToExpiredItems(UUID playerUUID, CollectableItem collectableItem) {
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
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to add item to expired items!");
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> removeFromExpiredItems(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        DELETE FROM `expired_items`
                        WHERE `playerUUID`=? AND `itemStack`=? AND `dateAdded` =?
                        LIMIT 1;""")) {
                    statement.setString(1, playerUUID.toString());
                    statement.setString(2, ItemSerializer.serialize(collectableItem.itemStack()));
                    statement.setLong(3, collectableItem.dateAdded());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to remove item from expired items!");
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<CollectableItem>> getExpiredItems(UUID playerUUID) {
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
                Fadah.getConsole().severe("Failed to get items from expired items!");
            }
            return retrievedData;
        });
    }

    @Override
    public CompletableFuture<Void> addListing(Listing listing) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO `listings`
                        (`uuid`,`ownerUUID`,`ownerName`, `category`, `creationDate`, `deletionDate`, `price`, `tax`, `itemStack`, `biddable`)
                        VALUES (?,?,?,?,?,?,?,?,?,?);""")) {
                    statement.setString(1, listing.getId().toString());
                    statement.setString(2, listing.getOwner().toString());
                    statement.setString(3, listing.getOwnerName());
                    statement.setString(4, listing.getCategoryID());
                    statement.setLong(5, listing.getCreationDate());
                    statement.setLong(6, listing.getDeletionDate());
                    statement.setDouble(7, listing.getPrice());
                    statement.setDouble(8, listing.getTax());
                    statement.setString(9, ItemSerializer.serialize(listing.getItemStack()));
                    statement.setBoolean(10, listing.isBiddable());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to add item to listings!");
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> removeListing(UUID id) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        DELETE FROM `listings`
                        WHERE `uuid`=?
                        LIMIT 1;""")) {
                    statement.setString(1, id.toString());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to remove item from listings!");
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<Listing>> getListings() {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            final List<Listing> retrievedData = Lists.newArrayList();
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT `uuid`, `ownerUUID`, `ownerName`, `category`, `creationDate`, `deletionDate`, `price`, `tax`, `itemStack`, `biddable`
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
                        final boolean biddable = resultSet.getBoolean("biddable");
                        retrievedData.add(new BukkitListing(id, ownerUUID, ownerName, itemStack, categoryID, price, tax, creationDate, deletionDate, biddable));
                    }
                    return retrievedData;
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to get items from listings!");
            }
            return retrievedData;
        });
    }

    @Override
    public CompletableFuture<Listing> getListing(UUID id) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT `ownerUUID`, `ownerName`, `category`, `creationDate`, `deletionDate`, `price`, `tax`, `itemStack`, `biddable`
                        FROM `listings`
                        WHERE `uuid`=?;"""
                )) {
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
                        final boolean biddable = resultSet.getBoolean("biddable");
                        return new BukkitListing(id, ownerUUID, ownerName, itemStack, categoryID, price, tax, creationDate, deletionDate, biddable);
                    }
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to get items from expired items!");
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> addToHistory(UUID playerUUID, HistoricItem historicItem) {
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
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                Fadah.getConsole().severe("Failed to add item to expired items!");
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<HistoricItem>> getHistory(UUID playerUUID) {
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
                Fadah.getConsole().log(Level.SEVERE, "Failed to get items from expired items!", e);
            }
            return retrievedData;
        });
    }
}
