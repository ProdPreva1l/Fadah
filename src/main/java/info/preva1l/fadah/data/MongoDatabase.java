package info.preva1l.fadah.data;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.ItemSerializer;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.mongo.CacheHandler;
import info.preva1l.fadah.utils.mongo.CollectionHelper;
import info.preva1l.fadah.utils.mongo.MongoConnectionHandler;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoDatabase implements Database {
    @Getter @Setter private boolean connected = false;
    private MongoConnectionHandler connectionHandler;
    private CacheHandler cacheHandler;
    @Getter private CollectionHelper collectionHelper;
    @Override
    public CompletableFuture<Void> connect() {
        return CompletableFuture.supplyAsync(()->{
            try {
                @NotNull String connectionURI = Config.DATABASE_URI.toString();
                @NotNull String database = Config.DATABASE.toString();
                Fadah.getConsole().info("Connecting to: " + connectionURI);
                connectionHandler = new MongoConnectionHandler(connectionURI, database);
                cacheHandler = new CacheHandler(connectionHandler);
                collectionHelper = new CollectionHelper(connectionHandler.getDatabase(), cacheHandler);
                setConnected(true);
            } catch (Exception e) {
                destroy();
                throw new IllegalStateException("Failed to establish a connection to the MongoDB database. " +
                        "Please check the supplied database credentials in the config file", e);
            }
            return null;
        });
    }

    @Override
    public void destroy() {
        setConnected(false);
        if (connectionHandler != null) connectionHandler.closeConnection();
        collectionHelper = null;
        cacheHandler = null;
        connectionHandler = null;
    }

    @Override
    public void addToCollectionBox(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        Document document = new Document("playerUUID", playerUUID)
                .append("itemStack", ItemSerializer.serialize(collectableItem.itemStack()))
                .append("dateAdded", collectableItem.dateAdded());
        TaskManager.Async.run(Fadah.getINSTANCE(), ()->collectionHelper.insertDocument("collection_box", document));
    }

    @Override
    public void removeFromCollectionBox(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        MongoCollection<Document> collection = collectionHelper.getCollection("collection_box");
        final Document listingDocument = collection.find().filter(Filters.eq("playerUUID", playerUUID))
                .filter(Filters.eq("itemStack", ItemSerializer.serialize(collectableItem.itemStack()))).first();
        if (listingDocument == null) return;
        TaskManager.Async.run(Fadah.getINSTANCE(), ()->collectionHelper.deleteDocument("collection_box", listingDocument));
    }

    @Override
    public List<CollectableItem> getCollectionBox(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return Collections.emptyList();
        }
        List<CollectableItem> list = new ArrayList<>();
        MongoCollection<Document> collection = collectionHelper.getCollection("collection_box");
        final FindIterable<Document> documents = collection.find().filter(Filters.eq("playerUUID", playerUUID));
        for (Document document : documents) {
            long dateAdded = document.getLong("dateAdded");
            ItemStack itemStack = ItemSerializer.deserialize(document.getString("itemStack"))[0];
            list.add(new CollectableItem(itemStack, dateAdded));
        }
        return list;
    }

    @Override
    public void loadCollectionBox(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        CollectionBoxCache.purgeCollectionbox(playerUUID);
        CollectionBoxCache.load(playerUUID, getCollectionBox(playerUUID));
    }

    @Override
    public void addToExpiredItems(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        Document document = new Document("playerUUID", playerUUID)
                .append("itemStack", ItemSerializer.serialize(collectableItem.itemStack()))
                .append("dateAdded", collectableItem.dateAdded());
        TaskManager.Async.run(Fadah.getINSTANCE(), ()->collectionHelper.insertDocument("expired_items", document));
    }

    @Override
    public void removeFromExpiredItems(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        MongoCollection<Document> collection = collectionHelper.getCollection("expired_items");
        final Document listingDocument = collection.find().filter(Filters.eq("playerUUID", playerUUID))
                .filter(Filters.eq("itemStack", ItemSerializer.serialize(collectableItem.itemStack()))).first();
        if (listingDocument == null) return;
        TaskManager.Async.run(Fadah.getINSTANCE(), ()->collectionHelper.deleteDocument("expired_items", listingDocument));
    }

    @Override
    public List<CollectableItem> getExpiredItems(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return Collections.emptyList();
        }
        List<CollectableItem> list = new ArrayList<>();
        MongoCollection<Document> collection = collectionHelper.getCollection("expired_items");
        final FindIterable<Document> documents = collection.find().filter(Filters.eq("playerUUID", playerUUID));
        for (Document document : documents) {
            long dateAdded = document.getLong("dateAdded");
            ItemStack itemStack = ItemSerializer.deserialize(document.getString("itemStack"))[0];
            list.add(new CollectableItem(itemStack, dateAdded));
        }
        return list;
    }

    @Override
    public void loadExpiredItems(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        ExpiredListingsCache.purgeExpiredListings(playerUUID);
        ExpiredListingsCache.load(playerUUID, getExpiredItems(playerUUID));
    }

    @Override
    public void addListing(Listing listing) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        Document document = new Document("uuid", listing.id())
                .append("ownerUUID", listing.owner())
                .append("ownerName", listing.ownerName())
                .append("category", listing.categoryID())
                .append("creationDate", listing.creationDate())
                .append("deletionDate", listing.deletionDate())
                .append("price", listing.price())
                .append("itemStack", ItemSerializer.serialize(listing.itemStack()));
        TaskManager.Async.run(Fadah.getINSTANCE(), ()->collectionHelper.insertDocument("listings", document));
    }

    @Override
    public void removeListing(UUID id) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        MongoCollection<Document> collection = collectionHelper.getCollection("listings");
        final Document listingDocument = collection.find().filter(Filters.eq("uuid", id)).first();
        if (listingDocument == null) return;
        TaskManager.Async.run(Fadah.getINSTANCE(), ()->collectionHelper.deleteDocument("listings", listingDocument));
    }

    @Override
    public void loadListings() {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        ListingCache.purgeListings();
        for (UUID id : getListingIDs()) {
            ListingCache.addListing(getListing(id));
        }
    }

    @Override
    public List<UUID> getListingIDs() {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return Collections.emptyList();
        }
        List<UUID> list = new ArrayList<>();
        MongoCollection<Document> collection = collectionHelper.getCollection("listings");
        for (Document doc : collection.find()) {
            list.add(doc.get("uuid", UUID.class));
        }
        return list;
    }


    @Override
    public Listing getListing(UUID id) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return null;
        }
        MongoCollection<Document> collection = collectionHelper.getCollection("listings");
        final Document listingDocument = collection.find().filter(Filters.eq("uuid", id)).first();
        if (listingDocument == null) return null;
        final UUID owner = listingDocument.get("ownerUUID", UUID.class);
        final String ownerName = listingDocument.getString("ownerName");
        final String category = listingDocument.getString("category");
        final long creationDate = listingDocument.getLong("creationDate");
        final long deletionDate = listingDocument.getLong("deletionDate");
        final double price = listingDocument.getDouble("price");
        final ItemStack itemStack = ItemSerializer.deserialize(listingDocument.getString("itemStack"))[0];
        return new Listing(id, owner, ownerName, itemStack, category, price, creationDate, deletionDate);
    }
}
