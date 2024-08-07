package info.preva1l.fadah.records;

import info.preva1l.fadah.cache.HistoricItemsCache;

import java.util.List;
import java.util.UUID;

public record History(
        UUID owner,
        List<HistoricItem> collectableItems
) {
    public static History of(UUID uuid) {
        return new History(uuid, HistoricItemsCache.getHistory(uuid));
    }
}
