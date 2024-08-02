package info.preva1l.fadah.data.handler;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.dao.Dao;
import lombok.Getter;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;

public class SQLiteHandler implements DatabaseHandler {
    private final Map<Class<?>, Dao<?>> daos = new HashMap<>();

    @Getter private boolean connected = false;

    private static final String DATABASE_FILE_NAME = "FadahData.db";
    private File databaseFile;
    private HikariDataSource dataSource;

    @Override
    public void connect() {
        databaseFile = new File(Fadah.getINSTANCE().getDataFolder(), DATABASE_FILE_NAME);

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
            dataSource = new HikariDataSource(config);
            this.backupFlatFile(databaseFile);

            final String[] databaseSchema = getSchemaStatements(String.format("database/%s_schema.sql", Config.DATABASE_TYPE.toDBTypeEnum().getId()));
            try (Statement statement = getConnection().createStatement()) {
                for (String tableCreationStatement : databaseSchema) {
                    statement.execute(tableCreationStatement);
                }
            } catch (SQLException e) {
                destroy();
                throw new IllegalStateException("Failed to create database tables.", e);
            }
        } catch (IOException e) {
            Fadah.getConsole().log(Level.SEVERE, "An exception occurred creating the database file", e);
            destroy();
        } catch (ClassNotFoundException e) {
            Fadah.getConsole().log(Level.SEVERE, "Failed to load the necessary SQLite driver", e);
            destroy();
        }
    }

    private void backupFlatFile(@NotNull File file) {
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
    public void destroy() {

    }

    @Override
    public <T> void registerDao(Class<T> clazz, Dao<T> dao) {
        daos.put(clazz, dao);
    }

    @Override
    public void wipeDatabase() {
        // nothing yet
    }

    @Override
    public <T> List<T> getAll(Class<T> clazz) {
        return (List<T>) getDao(clazz).getAll();
    }

    @Override
    public <T> Optional<T> get(Class<T> clazz, UUID id) {
        return (Optional<T>) getDao(clazz).get(id);
    }

    @Override
    public <T> void save(Class<T> clazz, T t) {
        getDao(clazz).save(t);
    }

    @Override
    public <T> void update(Class<T> clazz, T t, String[] params) {
        throw new NotImplementedException();
    }

    @Override
    public <T> void delete(Class<T> clazz, T t) {
        getDao(clazz).delete(t);
    }

    /**
     * Gets the DAO for a specific class.
     * @param clazz The class to get the DAO for.
     * @param <T> The type of the class.
     * @return The DAO for the specified class.
     */
    private <T> Dao<T> getDao(Class<?> clazz) {
        if(!daos.containsKey(clazz))
            throw new IllegalArgumentException("No DAO registered for class " + clazz.getName());
        return (Dao<T>) daos.get(clazz);
    }
}
