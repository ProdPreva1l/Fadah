package info.preva1l.fadah.data.handler;

import info.preva1l.fadah.data.dao.Dao;

public interface DatabaseHandler extends DataHandler {
    boolean isConnected();
    void connect();
    void destroy();
    <T> void registerDao(Class<T> clazz, Dao<T> dao);
    void wipeDatabase();
}