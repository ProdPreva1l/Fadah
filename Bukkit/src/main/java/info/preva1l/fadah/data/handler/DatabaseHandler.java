package info.preva1l.fadah.data.handler;

import java.util.UUID;

public interface DatabaseHandler extends DataHandler {
    boolean isConnected();
    void connect();
    void destroy();
    void registerDaos();
    void wipeDatabase();

    void fixData(UUID player);
}