package info.preva1l.fadah.cache;

import info.preva1l.fadah.records.CollectableItem;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class CollectionBoxCache {
    private final Map<UUID, List<CollectableItem>> collectionbox = new ConcurrentHashMap<>();

    public void addItem(UUID playerUUID, CollectableItem item) {
        collectionbox.compute(playerUUID, (uuid, items) -> {
            if (items == null) {
                items = new ArrayList<>();
            }
            items.add(item);

            items.sort(Comparator.comparingLong(CollectableItem::dateAdded).reversed());
            return items;
        });
    }

    public void removeItem(UUID playerUUID, CollectableItem item) {
        collectionbox.computeIfPresent(playerUUID, (uuid, items) -> {
            items.remove(item);
            return items;
        });
    }

    public void update(UUID playerUUID, List<CollectableItem> list) {
        list.sort(Comparator.comparingLong(CollectableItem::dateAdded).reversed());
        collectionbox.put(playerUUID, list);
    }

    public void invalidate(UUID playerUUID) {
        collectionbox.remove(playerUUID);
    }

    public List<CollectableItem> getCollectionBox(UUID playerUUID) {
        if (collectionbox.get(playerUUID) == null) {
            return List.of();
        }
        List<CollectableItem> ret = new ArrayList<>();
        collectionbox.computeIfPresent(playerUUID, (uuid, items) -> {
            ret.addAll(items);
            return items;
        });
        return ret;
    }

    public boolean doesItemExist(UUID playerUUID, CollectableItem e) {
        for (CollectableItem item : getCollectionBox(playerUUID)) { // todo: re-add strict checks
            if (item.equals(e)) return true;
        }
        return false;
    }
}
