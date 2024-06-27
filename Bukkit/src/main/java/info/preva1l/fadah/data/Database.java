package info.preva1l.fadah.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.records.Bid;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.Listing;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Database {
    Gson gson = new Gson();
    Type bidsType = new TypeToken<List<Bid>>() {}.getType();

    void connect();

    void destroy();

    default boolean loadPlayerData(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return false;
        }
        try {
            getExpiredItems(playerUUID).thenAccept(items -> ExpiredListingsCache.update(playerUUID, items));
            getHistory(playerUUID).thenAccept(items -> HistoricItemsCache.update(playerUUID, items));
            getCollectionBox(playerUUID).thenAccept(items -> CollectionBoxCache.update(playerUUID, items));
        } catch (Exception e){
            return false;
        }
        return true;
    }

    CompletableFuture<Void> addToCollectionBox(UUID playerUUID, CollectableItem collectableItem);

    CompletableFuture<Void> removeFromCollectionBox(UUID playerUUID, CollectableItem collectableItem);

    CompletableFuture<List<CollectableItem>> getCollectionBox(UUID playerUUID);

    CompletableFuture<Void> addToExpiredItems(UUID playerUUID, CollectableItem collectableItem);

    CompletableFuture<Void> removeFromExpiredItems(UUID playerUUID, CollectableItem collectableItem);

    CompletableFuture<List<CollectableItem>> getExpiredItems(UUID playerUUID);

    CompletableFuture<Void> addListing(Listing listing);

    CompletableFuture<Void> removeListing(UUID id);

    default void loadListings() {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return;
        }
        ListingCache.update();
    }

    CompletableFuture<List<Listing>> getListings();

    CompletableFuture<@Nullable Listing> getListing(UUID id);

    CompletableFuture<Void> addToHistory(UUID playerUUID, HistoricItem historicItem);

    CompletableFuture<List<HistoricItem>> getHistory(UUID playerUUID);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isConnected();
}