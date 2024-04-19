package info.preva1l.fadah.data;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Database {
    void connect();

    void destroy();

    void addToCollectionBox(UUID playerUUID, CollectableItem collectableItem);

    void removeFromCollectionBox(UUID playerUUID, CollectableItem collectableItem);

    CompletableFuture<List<CollectableItem>> getCollectionBox(UUID playerUUID);

    default void loadCollectionBox(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        CollectionBoxCache.purgeCollectionbox(playerUUID);
        getCollectionBox(playerUUID).thenAccept(box -> CollectionBoxCache.load(playerUUID, box));
    }

    void addToExpiredItems(UUID playerUUID, CollectableItem collectableItem);

    void removeFromExpiredItems(UUID playerUUID, CollectableItem collectableItem);

    CompletableFuture<List<CollectableItem>> getExpiredItems(UUID playerUUID);

    default void loadExpiredItems(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        ExpiredListingsCache.purgeExpiredListings(playerUUID);
        getExpiredItems(playerUUID).thenAccept(items -> ExpiredListingsCache.load(playerUUID, items));
    }

    void addListing(Listing listing);

    void removeListing(UUID id);

    default void loadListings() {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        ListingCache.purgeListings();
        getListingIDs().thenAccept(uuids -> {
            for (UUID id : uuids) {
                getListing(id).thenAccept(ListingCache::addListing);
            }
        });
    }

    CompletableFuture<List<UUID>> getListingIDs();

    CompletableFuture<Listing> getListing(UUID id);

    boolean isConnected();
}