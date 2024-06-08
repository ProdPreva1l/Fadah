package info.preva1l.fadah.data;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.Listing;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Database {
    CompletableFuture<Void> connect();

    void destroy();

    default CompletableFuture<Boolean> loadPlayerData(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(()->{
            try {
                CollectionBoxCache.purgeCollectionbox(playerUUID);
                ExpiredListingsCache.purgeExpiredListings(playerUUID);
                HistoricItemsCache.purgeHistory(playerUUID);
                getCollectionBox(playerUUID).thenAccept(box ->
                                CollectionBoxCache.load(playerUUID, box))
                        .thenAccept((v) ->
                                getExpiredItems(playerUUID).thenAccept(items ->
                                                ExpiredListingsCache.load(playerUUID, items))
                                .thenAccept((a) ->
                                getHistory(playerUUID).thenAccept(history ->
                                        HistoricItemsCache.load(playerUUID, history))));

            } catch (Exception e){
                return false;
            }
            return true;
        });
    }

    CompletableFuture<Void> addToCollectionBox(UUID playerUUID, CollectableItem collectableItem);

    CompletableFuture<Void> removeFromCollectionBox(UUID playerUUID, CollectableItem collectableItem);

    CompletableFuture<List<CollectableItem>> getCollectionBox(UUID playerUUID);

    default CompletableFuture<Void> loadCollectionBox(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            CollectionBoxCache.purgeCollectionbox(playerUUID);
            getCollectionBox(playerUUID).thenAccept(box -> CollectionBoxCache.load(playerUUID, box));
            return null;
        });
    }

    CompletableFuture<Void> addToExpiredItems(UUID playerUUID, CollectableItem collectableItem);

    CompletableFuture<Void> removeFromExpiredItems(UUID playerUUID, CollectableItem collectableItem);

    CompletableFuture<List<CollectableItem>> getExpiredItems(UUID playerUUID);

    default CompletableFuture<Void> loadExpiredItems(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(() -> null);
        }
        return CompletableFuture.supplyAsync(() -> {
            ExpiredListingsCache.purgeExpiredListings(playerUUID);
            getExpiredItems(playerUUID).thenAccept(items -> ExpiredListingsCache.load(playerUUID, items));
            return null;
        });
    }

    CompletableFuture<Void> addListing(Listing listing);

    CompletableFuture<Void> removeListing(UUID id);

    default CompletableFuture<Void> loadListings() {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        ListingCache.purgeListings();
        return getListings().thenAccept(listings -> listings.forEach(ListingCache::addListing));
    }

    CompletableFuture<List<Listing>> getListings();

    CompletableFuture<@Nullable Listing> getListing(UUID id);

    CompletableFuture<Void> addToHistory(UUID playerUUID, HistoricItem historicItem);

    CompletableFuture<List<HistoricItem>> getHistory(UUID playerUUID);

    default CompletableFuture<Void> loadHistory(UUID playerUUID) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.supplyAsync(()->null);
        }
        return CompletableFuture.supplyAsync(()-> {
            HistoricItemsCache.purgeHistory(playerUUID);
            getHistory(playerUUID).thenAccept(items -> HistoricItemsCache.load(playerUUID, items));
            return null;
        });
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isConnected();
}