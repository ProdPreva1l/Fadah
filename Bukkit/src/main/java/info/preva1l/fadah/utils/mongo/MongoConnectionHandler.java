package info.preva1l.fadah.utils.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.UuidRepresentation;

@Getter
public class MongoConnectionHandler {
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    /**
     * Initiate a connection to a Mongo Server
     *
     * @param uri    MongoDB Connection URI
     * @param dbName The database to connect to
     */
    public MongoConnectionHandler(String uri, String dbName) {
        final MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(dbName);
    }

    /**
     * Close the connection with the database
     */
    public void closeConnection() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }
}