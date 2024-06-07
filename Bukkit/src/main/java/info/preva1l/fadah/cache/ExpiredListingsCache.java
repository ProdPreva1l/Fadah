package info.preva1l.fadah.cache;

import info.preva1l.fadah.records.CollectableItem;
import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class ExpiredListingsCache {
    private final HashMap<UUID, List<CollectableItem>> expiredListings = new HashMap<>();

    public void addItem(UUID playerUUID, CollectableItem item) {
        List<CollectableItem> list = expiredListings.get(playerUUID);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(item);
        expiredListings.remove(playerUUID);
        expiredListings.put(playerUUID, list);
    }

    public void removeItem(UUID playerUUID, CollectableItem item) {
        List<CollectableItem> list = expiredListings.get(playerUUID);
        list.remove(item);
        expiredListings.remove(playerUUID);
        expiredListings.put(playerUUID, list);
    }

    public void purgeExpiredListings(UUID playerUUID) {
        expiredListings.remove(playerUUID);
    }

    public void load(UUID playerUUID, List<CollectableItem> list) {
        expiredListings.put(playerUUID, list);
    }

    public List<CollectableItem> getExpiredListings(UUID playerUUID) {
        if (expiredListings.get(playerUUID) == null || expiredListings.get(playerUUID).isEmpty()) {
            return new ArrayList<>();
        }
        List<CollectableItem> list = expiredListings.get(playerUUID);
        list.sort(Comparator.comparingLong(CollectableItem::dateAdded).reversed());
        return list;
    }
}
