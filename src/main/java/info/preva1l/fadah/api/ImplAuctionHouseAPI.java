package info.preva1l.fadah.api;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.*;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.Listing;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public class ImplAuctionHouseAPI implements AuctionHouseAPI {
    @Override
    public NamespacedKey getCustomItemNameSpacedKey() {
        return Fadah.getCustomItemKey();
    }

    @Override
    public void setCustomItemNameSpacedKey(NamespacedKey key) {
        Fadah.setCustomItemKey(key);
    }

    @Override
    public Listing getListing(UUID uuid) {
        return ListingCache.getListing(uuid);
    }

    @Override
    public Category getCategory(String id) {
        return CategoryCache.getCategory(id);
    }

    @Override
    public List<CollectableItem> getCollectionBox(OfflinePlayer offlinePlayer) {
        return CollectionBoxCache.getCollectionBox(offlinePlayer.getUniqueId());
    }

    @Override
    public List<CollectableItem> getCollectionBox(UUID uuid) {
        return CollectionBoxCache.getCollectionBox(uuid);
    }

    @Override
    public List<CollectableItem> getExpiredItems(OfflinePlayer offlinePlayer) {
        return ExpiredListingsCache.getExpiredListings(offlinePlayer.getUniqueId());
    }

    @Override
    public List<CollectableItem> getExpiredItems(UUID uuid) {
        return ExpiredListingsCache.getExpiredListings(uuid);
    }

    @Override
    public List<HistoricItem> getHistory(OfflinePlayer offlinePlayer) {
        return HistoricItemsCache.getHistory(offlinePlayer.getUniqueId());
    }

    @Override
    public List<HistoricItem> getHistory(UUID uuid) {
        return HistoricItemsCache.getHistory(uuid);
    }
}
