package info.preva1l.fadah.data;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.handler.DatabaseHandler;
import info.preva1l.fadah.data.handler.SQLiteHandler;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This is the manager for all database interactions.
 * There should be no case where this is modified.
 * Access this class via {@link DatabaseManager#getInstance()}
 */
public final class DatabaseManager {
    private static DatabaseManager instance;

    private final Map<DatabaseType, Class<? extends DatabaseHandler>> databaseHandlers = new HashMap<>();
    private final DatabaseHandler handler;

    private DatabaseManager() {
        Fadah.getConsole().info("Connecting to Database and populating caches...");
        databaseHandlers.put(DatabaseType.SQLITE, SQLiteHandler.class);

        this.handler = initHandler();
        Fadah.getConsole().info("Connected to Database and populated caches!");
    }

    private CompletableFuture<Void> initManager() {
        return CompletableFuture.supplyAsync(() -> {
            handler.connect();
            return null;
        });
    }

    public <T> CompletableFuture<List<T>> getAll(Class<T> clazz) {
        if (!handler.isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.supplyAsync(() -> handler.getAll(clazz));
    }

    public <T> CompletableFuture<Optional<T>> get(Class<T> clazz, UUID id) {
        if (!handler.isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.supplyAsync(() -> handler.get(clazz, id));
    }

    public <T> CompletableFuture<Void> save(Class<T> clazz, T t) {
        if (!handler.isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.save(clazz, t);
            return null;
        });
    }

    public <T> CompletableFuture<Void> delete(Class<T> clazz, T t) {
        if (!handler.isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.delete(clazz, t);
            return null;
        });
    }

    public <T> CompletableFuture<Void> update(Class<T> clazz, T t, String[] params) {
        if (!handler.isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.update(clazz, t, params);
            return null;
        });
    }

    public <T> CompletableFuture<Void> deleteSpecific(Class<T> clazz, T t, Object o) {
        if (!handler.isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.deleteSpecific(clazz, t, o);
            return null;
        });
    }

    public boolean isConnected() {
        return handler.isConnected();
    }

    public void shutdown() {
        handler.destroy();
    }

    private DatabaseHandler initHandler() {
        DatabaseType type = Config.DATABASE_TYPE.toDBTypeEnum();
        Fadah.getConsole().info("DB Type: %s".formatted(type.getFriendlyName()));
        try {
            Class<? extends DatabaseHandler> handlerClass = databaseHandlers.get(type);
            if (handlerClass == null) {
                throw new IllegalStateException("No handler for database type %s registered!".formatted(type.getFriendlyName()));
            }
            return handlerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
            instance.initManager().thenRun(ListingCache::update);
        }
        return instance;
    }
}
