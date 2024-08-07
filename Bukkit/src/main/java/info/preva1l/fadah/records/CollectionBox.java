package info.preva1l.fadah.records;

import info.preva1l.fadah.cache.CollectionBoxCache;

import java.util.List;
import java.util.UUID;

public record CollectionBox(
        UUID owner,
        List<CollectableItem> collectableItems
) {
    public static CollectionBox of(UUID uuid) {
        return new CollectionBox(uuid, CollectionBoxCache.getCollectionBox(uuid));
    }
}
