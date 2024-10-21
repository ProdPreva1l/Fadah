package info.preva1l.fadah.data.handler;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.DatabaseType;
import info.preva1l.fadah.data.dao.SqlDao;
import info.preva1l.fadah.data.dao.hikari.CollectionBoxHikariDao;
import info.preva1l.fadah.data.dao.hikari.ExpiredItemsHikariDao;
import info.preva1l.fadah.data.dao.hikari.HistoryHikariDao;
import info.preva1l.fadah.data.dao.hikari.ListingHikariDao;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.records.History;
import info.preva1l.fadah.records.Listing;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

public class HikariHandler implements DatabaseHandler {

    private static final String DATABASE_FILE_NAME = "FadahData.db";

    private final Config.Database conf = Config.i().getDatabase();
    private final Map<Class<?>, SqlDao<?>> daos = new HashMap<>();
    private final String driverClass;

    @Getter
    private boolean connected = false;
    private HikariDataSource hikari;

    public HikariHandler() {
        this.driverClass = switch (getType()) {
            case MONGO -> throw new IllegalArgumentException();
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case MARIADB -> "org.mariadb.jdbc.Driver";
            case SQLITE -> "org.sqlite.JDBC";
        };
    }

    @Override
    public void connect() {
        final HikariConfig config = new HikariConfig();
        config.setPoolName("FadahHikariPool");
        config.setDriverClassName(this.driverClass);

        if (getType().isLocal()) {
            final File file = new File(Fadah.getINSTANCE().getDataFolder(), DATABASE_FILE_NAME);
            try {
                if (file.createNewFile()) {
                    Fadah.getConsole().info("Created the " + getType().name() + " database file");
                }
            } catch (IOException e) {
                Fadah.getConsole().log(Level.SEVERE, "Cannot create database file", e);
            }

            config.setJdbcUrl(String.format("jdbc:%s:%s", getType().getId(), file.getAbsolutePath()));
            config.setConnectionTestQuery("SELECT 1");
            config.setMaxLifetime(60000);
            config.setIdleTimeout(45000);
            config.setMaximumPoolSize(50);

            this.hikari = new HikariDataSource(config);

            backup(file);
        } else {
            config.setJdbcUrl(conf.getUri());
            if (!conf.getUri().contains("@")) {
                config.setUsername(conf.getUsername());
                config.setPassword(conf.getPassword());
            }

            config.setMaximumPoolSize(conf.getAdvanced().getPoolSize());
            config.setMinimumIdle(conf.getAdvanced().getMinIdle());
            config.setMaxLifetime(conf.getAdvanced().getMaxLifetime());
            config.setKeepaliveTime(conf.getAdvanced().getKeepaliveTime());
            config.setConnectionTimeout(conf.getAdvanced().getConnectionTimeout());

            final Properties properties = new Properties();
            properties.put("cachePrepStmts", "true");
            properties.put("prepStmtCacheSize", "250");
            properties.put("prepStmtCacheSqlLimit", "2048");
            properties.put("useServerPrepStmts", "true");
            properties.put("useLocalSessionState", "true");
            properties.put("useLocalTransactionState", "true");
            properties.put("rewriteBatchedStatements", "true");
            properties.put("cacheResultSetMetadata", "true");
            properties.put("cacheServerConfiguration", "true");
            properties.put("elideSetAutoCommits", "true");
            properties.put("maintainTimeStats", "false");

            config.setDataSourceProperties(properties);

            this.hikari = new HikariDataSource(config);
        }

        registerDaos();

        // Execute schema
        connect(con -> {
            if (isTablePresent(con, "items")) {
                return;
            }

            final String[] schema = getSchema();
            try (Statement stmt = con.createStatement()) {
                for (String sql : schema) {
                    stmt.addBatch(sql);
                }

                stmt.executeBatch();
            }

            migrateListings(con);
            migrateCollectionBox(con);
            migrateExpiredItems(con);
        }, "Failed to create database table.");

        connected = true;
    }

    private void migrateListings(@NotNull Connection con) throws SQLException {
        if (!isTablePresent(con, "listings")) {
            return;
        }

        try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `listings`"); PreparedStatement insert = con.prepareStatement(getDao(Listing.class).sql(SqlDao.Statement.INSERT))) {
            final ResultSet result = stmt.executeQuery();

            while (result.next()) {
                insert.setString(1, result.getString("uuid"));
                insert.setString(2, result.getString("ownerUUID"));
                insert.setString(3, result.getString("ownerName"));
                insert.setString(4, result.getString("itemStack"));
                insert.setString(5, result.getString("category"));
                insert.setDouble(6, result.getDouble("price"));
                insert.setDouble(7, result.getDouble("tax"));
                insert.setLong(8, result.getLong("creationDate"));
                insert.setBoolean(9, result.getBoolean("biddable"));
                insert.setString(10, "");
                insert.setLong(11, Instant.now().toEpochMilli());

                insert.addBatch();
            }

            insert.executeBatch();
        }
    }

    private void migrateCollectionBox(@NotNull Connection con) throws SQLException {
        if (!isTablePresent(con, "collection_box")) {
            return;
        }

        final long time = Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli();
        try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `collection_box`"); PreparedStatement insert = con.prepareStatement(getDao(CollectionBox.class).sql(SqlDao.Statement.INSERT))) {
            final ResultSet result = stmt.executeQuery();

            while (result.next()) {
                insert.setString(1, UUID.randomUUID().toString());
                insert.setString(2, DataHandler.DUMMY_ID.toString());
                insert.setString(3, result.getString("playerUUID"));
                insert.setString(4, result.getString("itemStack"));
                insert.setLong(5, time);
                insert.setLong(6, result.getLong("dateAdded"));
                insert.setBoolean(7, false);

                insert.addBatch();
            }

            insert.executeBatch();
        }
    }

    private void migrateExpiredItems(@NotNull Connection con) throws SQLException {
        if (!isTablePresent(con, "expired_items")) {
            return;
        }

        final long time = Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli();
        try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM `expired_items`"); PreparedStatement insert = con.prepareStatement(getDao(ExpiredItems.class).sql(SqlDao.Statement.INSERT))) {
            final ResultSet result = stmt.executeQuery();

            while (result.next()) {
                insert.setString(1, UUID.randomUUID().toString());
                insert.setString(2, result.getString("playerUUID"));
                insert.setString(3, result.getString("itemStack"));
                insert.setLong(4, time);
                insert.setLong(5, result.getLong("dateAdded"));
                insert.setBoolean(6, false);

                insert.addBatch();
            }

            insert.executeBatch();
        }
    }

    @Override
    public void destroy() {
        if (hikari != null) {
            hikari.close();
        }
    }

    @Override
    public void registerDaos() {
        daos.put(CollectionBox.class, new CollectionBoxHikariDao(this));
        daos.put(ExpiredItems.class, new ExpiredItemsHikariDao(this));
        daos.put(History.class, new HistoryHikariDao(this));
        daos.put(Listing.class, new ListingHikariDao(this));
    }

    @Override
    public void wipeDatabase() {
        // nothing yet
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getAll(Class<T> clazz) {
        return (List<T>) getDao(clazz).getAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Class<T> clazz, UUID id) {
        return (Optional<T>) getDao(clazz).get(id);
    }

    @Override
    public <T> void save(Class<T> clazz, T t) {
        getDao(clazz).save(t);
    }

    @Override
    public <T> void update(Class<T> clazz, T t, String[] params) {
        getDao(clazz).update(t, params);
    }

    @Override
    public <T> void delete(Class<T> clazz, T t) {
        getDao(clazz).delete(t);
    }

    @Override
    public <T> void deleteSpecific(Class<T> clazz, T t, Object o) {
        getDao(clazz).deleteSpecific(t, o);
    }

    @NotNull
    public DatabaseType getType() {
        return conf.getType();
    }

    private Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }

    @NotNull
    private String[] getSchema() {
        try {
            final InputStream input = Fadah.getINSTANCE().getResource(String.format("database/%s_schema.sql", getType().getId()));
            Objects.requireNonNull(input);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).split(";");
        }  catch (IOException e) {
            throw new RuntimeException("Cannot get database schema for " + getType().name() + " database");
        }
    }

    /**
     * Gets the DAO for a specific class.
     *
     * @param clazz The class to get the DAO for.
     * @param <T>   The type of the class.
     * @return The DAO for the specified class.
     */
    @SuppressWarnings("unchecked")
    private <T> SqlDao<T> getDao(@NotNull Class<?> clazz) {
        if (!daos.containsKey(clazz))
            throw new IllegalArgumentException("No DAO registered for class " + clazz.getName());
        return (SqlDao<T>) daos.get(clazz);
    }

    public void connect(@NotNull SqlConsumer consumer) {
        connect(consumer, "Cannot execute database connection");
    }

    public void connect(@NotNull SqlConsumer consumer, @NotNull String msg) {
        try (Connection connection = hikari.getConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            Fadah.getConsole().log(Level.WARNING, msg, e);
        }
    }

    @Nullable
    @Contract("_, !null -> !null")
    public <T> T connect(@NotNull SqlFunction<T> function, @Nullable T def) {
        return connect(function, "Cannot execute database connection", def);
    }

    @Contract("_, _, !null -> !null")
    public <T> T connect(@NotNull SqlFunction<T> function, @NotNull String msg, @Nullable T def) {
        try (Connection connection = hikari.getConnection()) {
            return function.apply(connection);
        } catch (SQLException e) {
            Fadah.getConsole().log(Level.WARNING, msg, e);
        }
        return def;
    }

    private static void backup(@NotNull File file) {
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

    private static boolean isTablePresent(@NotNull Connection con, @NotNull String tableName) throws SQLException {
        try (ResultSet set = con.getMetaData().getTables(con.getCatalog(), null, "%", null)) {
            while (set.next()) {
                if (set.getString(3).equalsIgnoreCase(tableName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface SqlConsumer {
        void accept(@NotNull Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(@NotNull Connection connection) throws SQLException;
    }
}
