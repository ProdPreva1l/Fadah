package info.preva1l.fadah.utils.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

public class CollectionHelper {
    private final MongoDatabase database;
    private final CacheHandler cacheHandler;

    public CollectionHelper(MongoDatabase database, CacheHandler cacheHandler) {
        this.database = database;
        this.cacheHandler = cacheHandler;
    }

    public void createCollection(String collectionName) {
        database.createCollection(collectionName);
        cacheHandler.updateCache(collectionName);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        if (cacheHandler.getCachedCollection(collectionName) == null) createCollection(collectionName);
        return cacheHandler.getCachedCollection(collectionName);
    }

    public void insertDocument(String collectionName, Document document) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);
        cacheHandler.updateCache(collectionName);
    }

    public void updateDocument(String collectionName, Document document, Bson updates) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.updateOne(document, updates);
        cacheHandler.updateCache(collectionName);
    }

    public void deleteDocument(String collectionName, Document document) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.deleteOne(document);
        cacheHandler.removeFromCache(collectionName);
    }
}
