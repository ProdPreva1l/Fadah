package info.preva1l.fadah.records;

import info.preva1l.fadah.cache.ExpiredListingsCache;

import java.util.List;
import java.util.UUID;

public record ExpiredItems(
        UUID owner,
        List<CollectableItem> collectableItems
) {
    public static ExpiredItems of(UUID uuid) {
        return new ExpiredItems(uuid, ExpiredListingsCache.getExpiredListings(uuid));
    }
}
