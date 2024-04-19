package info.preva1l.fadah.api;

import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.CollectableItem;
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
     */
    void setCustomItemNameSpacedKey(NamespacedKey key);

    /**
     * Get a listing
     *
     * @param uuid a valid listing uuid
     * @return the listing or null if none found
     */
    Listing getListing(UUID uuid);

    /**
     * Get a category
     *
     * @param id a valid category id
     * @return the category or null if none found
     */
    Category getCategory(String id);

    /**
     * Get a players collection box
     *
     * @param offlinePlayer a player
     * @return the collection box or null if no items found for that player
     */
    List<CollectableItem> getCollectionBox(OfflinePlayer offlinePlayer);

    /**
     * Get a players collection box
     *
     * @param uuid a players uuid
     * @return the collection box or null if no items found for that player
     */
    List<CollectableItem> getCollectionBox(UUID uuid);

    /**
     * Get a players expired items
     *
     * @param offlinePlayer a players uuid
     * @return the expired items or null if no items found for that player
     */
    List<CollectableItem> getExpiredItems(OfflinePlayer offlinePlayer);

    /**
     * Get a players expired items
     *
     * @param uuid a players uuid
     * @return the expired items or null if no items found for that player
     */
    List<CollectableItem> getExpiredItems(UUID uuid);
}
