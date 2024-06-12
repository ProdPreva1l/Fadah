package info.preva1l.fadah.data;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.BukkitListing;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.ItemSerializer;
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
    @Getter
    @Setter
    private boolean connected = false;
    private MongoConnectionHandler connectionHandler;
    private CacheHandler cacheHandler;
    @Getter
    private CollectionHelper collectionHelper;

    @Override
    public CompletableFuture<Void> connect() {
        return CompletableFuture.supplyAsync(() -> {
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
            this.loadListings();
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
    public CompletableFuture<Void> addToCollectionBox(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            Document document = new Document("playerUUID", playerUUID)
                    .append("itemStack", ItemSerializer.serialize(collectableItem.itemStack()))
                    .append("dateAdded", collectableItem.dateAdded());
            collectionHelper.insertDocument("collection_box", document);
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
            MongoCollection<Document> collection = collectionHelper.getCollection("collection_box");
            final Document listingDocument = collection.find().filter(Filters.eq("playerUUID", playerUUID))
                    .filter(Filters.eq("itemStack", ItemSerializer.serialize(collectableItem.itemStack()))).first();
            if (listingDocument == null) return null;
            collectionHelper.deleteDocument("collection_box", listingDocument);
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
            List<CollectableItem> list = new ArrayList<>();
            MongoCollection<Document> collection = collectionHelper.getCollection("collection_box");
            final FindIterable<Document> documents = collection.find().filter(Filters.eq("playerUUID", playerUUID));
            for (Document document : documents) {
                long dateAdded = document.getLong("dateAdded");
                ItemStack itemStack = ItemSerializer.deserialize(document.getString("itemStack"))[0];
                list.add(new CollectableItem(itemStack, dateAdded));
            }
            return list;
        });
    }

    @Override
    public CompletableFuture<Void> addToExpiredItems(UUID playerUUID, CollectableItem collectableItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            Document document = new Document("playerUUID", playerUUID)
                    .append("itemStack", ItemSerializer.serialize(collectableItem.itemStack()))
                    .append("dateAdded", collectableItem.dateAdded());
            collectionHelper.insertDocument("expired_items", document);
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
            MongoCollection<Document> collection = collectionHelper.getCollection("expired_items");
            final Document listingDocument = collection.find().filter(Filters.eq("playerUUID", playerUUID))
                    .filter(Filters.eq("itemStack", ItemSerializer.serialize(collectableItem.itemStack()))).first();
            if (listingDocument == null) return null;
            collectionHelper.deleteDocument("expired_items", listingDocument);
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
            List<CollectableItem> list = new ArrayList<>();
            MongoCollection<Document> collection = collectionHelper.getCollection("expired_items");
            final FindIterable<Document> documents = collection.find().filter(Filters.eq("playerUUID", playerUUID));
            for (Document document : documents) {
                long dateAdded = document.getLong("dateAdded");
                ItemStack itemStack = ItemSerializer.deserialize(document.getString("itemStack"))[0];
                list.add(new CollectableItem(itemStack, dateAdded));
            }
            return list;
        });
    }

    @Override
    public CompletableFuture<Void> addListing(Listing listing) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            Document document = new Document("uuid", listing.getId())
                    .append("ownerUUID", listing.getOwner())
                    .append("ownerName", listing.getOwnerName())
                    .append("category", listing.getCategoryID())
                    .append("creationDate", listing.getCreationDate())
                    .append("deletionDate", listing.getDeletionDate())
                    .append("price", listing.getPrice())
                    .append("tax", listing.getTax())
                    .append("itemStack", ItemSerializer.serialize(listing.getItemStack()));
            collectionHelper.insertDocument("listings", document);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> removeListing(UUID id) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            MongoCollection<Document> collection = collectionHelper.getCollection("listings");
            final Document listingDocument = collection.find().filter(Filters.eq("uuid", id)).first();
            if (listingDocument == null) return null;
            collectionHelper.deleteDocument("listings", listingDocument);
            return null;
        });
    }

    @Override
    public CompletableFuture<List<Listing>> getListings() {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(Collections::emptyList);
        }
        return CompletableFuture.supplyAsync(() -> {
            List<Listing> list = new ArrayList<>();
            MongoCollection<Document> collection = collectionHelper.getCollection("listings");
            for (Document doc : collection.find()) {
                final UUID id = doc.get("uuid", UUID.class);
                final UUID owner = doc.get("ownerUUID", UUID.class);
                final String ownerName = doc.getString("ownerName");
                final String category = doc.getString("category");
                final long creationDate = doc.getLong("creationDate");
                final long deletionDate = doc.getLong("deletionDate");
                final double price = doc.getDouble("price");
                final double tax = doc.getDouble("tax");
                final ItemStack itemStack = ItemSerializer.deserialize(doc.getString("itemStack"))[0];
                list.add(new BukkitListing(id, owner, ownerName, itemStack, category, price, tax, creationDate, deletionDate));
            }
            return list;
        });
    }


    @Override
    public CompletableFuture<Listing> getListing(UUID id) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            MongoCollection<Document> collection = collectionHelper.getCollection("listings");
            final Document listingDocument = collection.find().filter(Filters.eq("uuid", id)).first();
            if (listingDocument == null) return null;
            final UUID owner = listingDocument.get("ownerUUID", UUID.class);
            final String ownerName = listingDocument.getString("ownerName");
            final String category = listingDocument.getString("category");
            final long creationDate = listingDocument.getLong("creationDate");
            final long deletionDate = listingDocument.getLong("deletionDate");
            final double price = listingDocument.getDouble("price");
            final double tax = listingDocument.getDouble("tax");
            final ItemStack itemStack = ItemSerializer.deserialize(listingDocument.getString("itemStack"))[0];
            return new BukkitListing(id, owner, ownerName, itemStack, category, price, tax, creationDate, deletionDate);
        });
    }

    @Override
    public CompletableFuture<Void> addToHistory(UUID playerUUID, HistoricItem historicItem) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(() -> {
            Document document = new Document("playerUUID", playerUUID)
                    .append("itemStack", ItemSerializer.serialize(historicItem.itemStack()))
                    .append("loggedDate", historicItem.loggedDate())
                    .append("loggedAction", historicItem.action().ordinal())
                    .append("price", historicItem.price())
                    .append("purchaserUUID", historicItem.purchaserUUID());
            collectionHelper.insertDocument("history", document);
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
            List<HistoricItem> list = new ArrayList<>();
            MongoCollection<Document> collection = collectionHelper.getCollection("history");
            final FindIterable<Document> documents = collection.find().filter(Filters.eq("playerUUID", playerUUID));
            for (Document document : documents) {
                final long loggedDate = document.getLong("loggedDate");
                final double price = document.getDouble("price");
                final ItemStack itemStack = ItemSerializer.deserialize(document.getString("itemStack"))[0];
                final HistoricItem.LoggedAction loggedAction = HistoricItem.LoggedAction.values()[document.getInteger("loggedAction")];
                final UUID purchaserUUID = document.getString("purchaserUUID") == null ? null : UUID.fromString(document.getString("purchaserUUID"));
                list.add(new HistoricItem(playerUUID, loggedDate, loggedAction, itemStack, price, purchaserUUID));
            }
            return list;
        });
    }
}
