package info.preva1l.fadah.data;

import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;

import java.util.List;
import java.util.UUID;

public interface Database {
    void connect();
    void destroy();

    void addToCollectionBox(UUID playerUUID, CollectableItem collectableItem);
    void removeFromCollectionBox(UUID playerUUID, CollectableItem collectableItem);
    List<CollectableItem> getCollectionBox(UUID playerUUID);
    void loadCollectionBox(UUID playerUUID);

    void addToExpiredItems(UUID playerUUID, CollectableItem collectableItem);
    void removeFromExpiredItems(UUID playerUUID, CollectableItem collectableItem);
    List<CollectableItem> getExpiredItems(UUID playerUUID);
    void loadExpiredItems(UUID playerUUID);

    void addListing(Listing listing);
    void removeListing(UUID id);
    void loadListings();
    List<UUID> getListingIDs();
    Listing getListing(UUID id);
}