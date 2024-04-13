package info.preva1l.fadah.cache;

import info.preva1l.fadah.records.CollectableItem;
import lombok.experimental.UtilityClass;
import java.util.*;

@UtilityClass
public class CollectionBoxCache {
    private final HashMap<UUID, List<CollectableItem>> collectionbox = new HashMap<>();
    public void addItem(UUID playerUUID, CollectableItem item) {
        List<CollectableItem> list = collectionbox.get(playerUUID);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(item);
        collectionbox.remove(playerUUID);
        collectionbox.put(playerUUID, list);
    }
    public void removeItem(UUID playerUUID, CollectableItem item) {
        List<CollectableItem> list = collectionbox.get(playerUUID);
        list.remove(item);
        collectionbox.remove(playerUUID);
        collectionbox.put(playerUUID, list);
    }
    public void purgeCollectionbox(UUID playerUUID) {
        collectionbox.remove(playerUUID);
    }
    public void load(UUID playerUUID, List<CollectableItem> list) {
        collectionbox.put(playerUUID, list);
    }
    public List<CollectableItem> getCollectionBox(UUID playerUUID) {
        if (collectionbox.get(playerUUID) == null || collectionbox.get(playerUUID).isEmpty()) {
            return new ArrayList<>();
        }
        List<CollectableItem> list = collectionbox.get(playerUUID);
        list.sort(Comparator.comparingLong(CollectableItem::dateAdded).reversed());
        return list;
    }
}
