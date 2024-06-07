package info.preva1l.fadah.utils.mongo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

public class CacheHandler {
    private final MongoConnectionHandler db;
    private final Cache<String, MongoCollection<Document>> collectionCache = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build();

    public CacheHandler(MongoConnectionHandler db) {
        this.db = db;
    }

    public void updateCache(String collectionName) {
        MongoDatabase database = db.getDatabase();
        collectionCache.put(collectionName, database.getCollection(collectionName));
    }

    public void removeFromCache(String collectionName) {
        MongoCollection<Document> cachedCollection = collectionCache.getIfPresent(collectionName);
        if (cachedCollection != null) {
            MongoDatabase database = db.getDatabase();
            collectionCache.asMap().remove(collectionName, database.getCollection(collectionName));
        }
    }

    public MongoCollection<Document> getCachedCollection(String collectionName) {
        return collectionCache.getIfPresent(collectionName);
    }
}
