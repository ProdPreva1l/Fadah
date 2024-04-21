package info.preva1l.fadah.cache;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.records.HistoricItem;
import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class HistoricItemsCache {
    private final HashMap<UUID, List<HistoricItem>> historicItems = new HashMap<>();

    public void addLog(UUID playerUUID, HistoricItem item) {
        List<HistoricItem> list = historicItems.get(playerUUID);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(item);
        historicItems.remove(playerUUID);
        historicItems.put(playerUUID, list);
    }

    public void purgeHistory(UUID playerUUID) {
        historicItems.remove(playerUUID);
    }

    public void load(UUID playerUUID, List<HistoricItem> list) {
        historicItems.put(playerUUID, list);
    }

    public List<HistoricItem> getHistory(UUID playerUUID) {
        if (historicItems.get(playerUUID) == null || historicItems.get(playerUUID).isEmpty()) {
            return new ArrayList<>();
        }
        List<HistoricItem> list = historicItems.get(playerUUID);
        list.sort(Comparator.comparingLong(HistoricItem::loggedDate).reversed());
        return new ArrayList<>(list);
    }

    public boolean playerExists(UUID uuid) {
        return !Fadah.getINSTANCE().getDatabase().getHistory(uuid).join().isEmpty();
    }
    public boolean playerExistsInCache(UUID uuid) {
        return !historicItems.containsKey(uuid);
    }
}
