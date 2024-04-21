package info.preva1l.fadah.api;

import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.Listing;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public interface AuctionHouseAPI {
    /**
     * Get the custom item filtering namespacedkey
     *
     * @return namespacedkey
     */
    NamespacedKey getCustomItemNameSpacedKey();

    /**
     * Set the custom item filtering namespacedkey
     *
     * @param key namespacedkey
     * @since 1.0
     */
    void setCustomItemNameSpacedKey(NamespacedKey key);

    /**
     * Get a listing
     *
     * @param uuid a valid listing uuid
     * @return the listing or null if none found
     * @since 1.0
     */
    Listing getListing(UUID uuid);

    /**
     * Get a category
     *
     * @param id a valid category id
     * @return the category or null if none found
     * @since 1.0
     */
    Category getCategory(String id);

    /**
     * Get a players collection box
     *
     * @param offlinePlayer a player
     * @return the collection box or null if no items found for that player
     * @since 1.0
     */
    List<CollectableItem> getCollectionBox(OfflinePlayer offlinePlayer);

    /**
     * Get a players collection box
     *
     * @param uuid a players uuid
     * @return the collection box or null if no items found for that player
     * @since 1.0
     */
    List<CollectableItem> getCollectionBox(UUID uuid);

    /**
     * Get a players expired items
     *
     * @param offlinePlayer a player
     * @return the expired items or null if no items found for that player
     * @since 1.0
     */
    List<CollectableItem> getExpiredItems(OfflinePlayer offlinePlayer);

    /**
     * Get a players expired items
     *
     * @param uuid a players uuid
     * @return the expired items or null if no items found for that player
     * @since 1.0
     */
    List<CollectableItem> getExpiredItems(UUID uuid);

    /**
     * Get a players history
     *
     * @param offlinePlayer a player
     * @return the players history, ordered from newest to oldest
     */
    List<HistoricItem> getHistory(OfflinePlayer offlinePlayer);
    /**
     * Get a players history
     *
     * @param uuid a player uuid
     * @return the players history, ordered from newest to oldest
     */
    List<HistoricItem> getHistory(UUID uuid);
}
