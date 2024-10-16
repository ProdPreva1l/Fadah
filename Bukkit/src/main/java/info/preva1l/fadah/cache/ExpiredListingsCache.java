package info.preva1l.fadah.cache;

import info.preva1l.fadah.records.CollectableItem;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class ExpiredListingsCache {
    private final Map<UUID, List<CollectableItem>> expiredListings = new ConcurrentHashMap<>();

    public void addItem(UUID playerUUID, CollectableItem item) {
        expiredListings.compute(playerUUID, (uuid, items) -> {
            if (items == null) {
                items = new ArrayList<>();
            }
            items.add(item);

            items.sort(Comparator.comparingLong(CollectableItem::dateAdded).reversed());
            return items;
        });
    }

    public void removeItem(UUID playerUUID, CollectableItem item) {
        expiredListings.computeIfPresent(playerUUID, (uuid, items) -> {
            items.remove(item);
            return items;
        });
    }

    public void update(UUID playerUUID, List<CollectableItem> list) {
        list.sort(Comparator.comparingLong(CollectableItem::dateAdded).reversed());
        expiredListings.put(playerUUID, list);
    }

    public void invalidate(UUID playerUUID) {
        expiredListings.remove(playerUUID);
    }

    public List<CollectableItem> getExpiredListings(UUID playerUUID) {
        if (expiredListings.get(playerUUID) == null) {
            return new ArrayList<>();
        }
        List<CollectableItem> ret = new ArrayList<>();
        expiredListings.computeIfPresent(playerUUID, (uuid, items) -> {
            ret.addAll(items);
            return items;
        });
        return ret;
    }

    public boolean doesItemExist(UUID playerUUID, CollectableItem e) {
        for (CollectableItem item : getExpiredListings(playerUUID)) { // todo: readd strict checks
            if (item.equals(e)) return true;
        }
        return false;
    }
}
