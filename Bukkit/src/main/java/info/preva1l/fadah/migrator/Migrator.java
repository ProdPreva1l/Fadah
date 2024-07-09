package info.preva1l.fadah.migrator;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Migrator {
    String getMigratorName();

    default CompletableFuture<Void> startMigration(Fadah plugin) {
        return CompletableFuture.supplyAsync(() -> {
            int migratedListings = 0;
            List<Listing> listings = migrateListings();

            for (Listing listing : listings) {
                ListingCache.addListing(listing);
                plugin.getDatabase().addListing(listing);
                migratedListings++;
            }
            Fadah.getConsole().info("Migrated %s listings!".formatted(migratedListings));

            int migratedCollectionBoxes = 0;
            List<Map<CollectableItem, UUID>> collectionBoxes = migrateCollectionBoxes();

            for (Map<CollectableItem, UUID> collectionBox : collectionBoxes) {
                for (CollectableItem item : collectionBox.keySet()) {
                    UUID player = collectionBox.get(item);
                    CollectionBoxCache.addItem(player, item);
                    plugin.getDatabase().addToCollectionBox(player, item);
                }
                migratedCollectionBoxes++;
            }
            Fadah.getConsole().info("Migrated %s collection boxes!".formatted(migratedCollectionBoxes));

            int migratedExpiredItems = 0;
            List<Map<CollectableItem, UUID>> expiredItems = migrateExpiredListings();

            for (Map<CollectableItem, UUID> collectionBox : collectionBoxes) {
                for (CollectableItem item : collectionBox.keySet()) {
                    UUID player = collectionBox.get(item);
                    ExpiredListingsCache.addItem(player, item);
                    plugin.getDatabase().addToExpiredItems(player, item);
                }
                migratedExpiredItems++;
            }
            Fadah.getConsole().info("Migrated %s players expired items!".formatted(migratedExpiredItems));
            return null;
        });
    }

    List<Listing> migrateListings();
    List<Map<CollectableItem, UUID>> migrateCollectionBoxes();
    List<Map<CollectableItem, UUID>> migrateExpiredListings();
}
