package info.preva1l.fadah.cache;

import info.preva1l.fadah.records.HistoricItem;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class HistoricItemsCache {
    private final Map<UUID, List<HistoricItem>> historicItems = new ConcurrentHashMap<>();

    public void addLog(UUID playerUUID, HistoricItem item) {
        historicItems.compute(playerUUID, (uuid, items) -> {
            if (items == null) {
                items = new ArrayList<>();
            }
            items.add(item);

            items.sort(Comparator.comparingLong(HistoricItem::getLoggedDate).reversed());
            return items;
        });
    }

    public void update(UUID playerUUID, List<HistoricItem> list) {
        list.sort(Comparator.comparingLong(HistoricItem::getLoggedDate).reversed());
        historicItems.put(playerUUID, list);
    }

    public void invalidate(UUID playerUUID) {
        historicItems.remove(playerUUID);
    }

    public List<HistoricItem> getHistory(UUID playerUUID) {
        if (historicItems.get(playerUUID) == null) {
            return new ArrayList<>();
        }
        List<HistoricItem> ret = new ArrayList<>();
        historicItems.computeIfPresent(playerUUID, (uuid, items) -> {
            ret.addAll(items);
            return items;
        });
        return ret;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean playerExists(UUID uuid) {
        return !getHistory(uuid).isEmpty();
    }
}