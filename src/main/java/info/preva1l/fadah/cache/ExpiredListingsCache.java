package info.preva1l.fadah.cache;

import info.preva1l.fadah.records.CollectableItem;
import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class ExpiredListingsCache {
    private final HashMap<UUID, List<CollectableItem>> expiredLisings = new HashMap<>();
    public void addItem(UUID playerUUID, CollectableItem item) {
        List<CollectableItem> list = expiredLisings.get(playerUUID);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(item);
        expiredLisings.remove(playerUUID);
        expiredLisings.put(playerUUID, list);
    }
    public void removeItem(UUID playerUUID, CollectableItem item) {
        List<CollectableItem> list = expiredLisings.get(playerUUID);
        list.remove(item);
        expiredLisings.remove(playerUUID);
        expiredLisings.put(playerUUID, list);
    }
    public void purgeExpiredListings(UUID playerUUID) {
        expiredLisings.remove(playerUUID);
    }
    public void load(UUID playerUUID, List<CollectableItem> list) {
        expiredLisings.put(playerUUID, list);
    }
    public List<CollectableItem> getExpiredListings(UUID playerUUID) {
        if (expiredLisings.get(playerUUID) == null || expiredLisings.get(playerUUID).isEmpty()) {
            return new ArrayList<>();
        }
        List<CollectableItem> list = expiredLisings.get(playerUUID);
        list.sort(Comparator.comparingLong(CollectableItem::dateAdded).reversed());
        return list;
    }
}
