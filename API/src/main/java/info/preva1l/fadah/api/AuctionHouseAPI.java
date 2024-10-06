package info.preva1l.fadah.api;

import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.Listing;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public abstract class AuctionHouseAPI {
    @Getter private static AuctionHouseAPI instance = null;

    /**
     * Get the custom item filtering namespacedkey
     *
     * @return namespacedkey
     */
    public abstract NamespacedKey getCustomItemNameSpacedKey();

    /**
     * Set the custom item filtering namespacedkey
     *
     * @param key namespacedkey
     * @since 1.0
     */
    public abstract void setCustomItemNameSpacedKey(NamespacedKey key);

    /**
     * Get a listing
     *
     * @param uuid a valid listing uuid
     * @return the listing or null if none found
     * @since 1.0
     */
    public abstract Listing getListing(UUID uuid);

    /**
     * Get a category
     *
     * @param id a valid category id
     * @return the category or null if none found
     * @since 1.0
     */
    public abstract Category getCategory(String id);

    /**
     * Get a players collection box
     *
     * @param offlinePlayer a player
     * @return the collection box or null if no items found for that player
     * @since 1.0
     */
    public abstract List<CollectableItem> getCollectionBox(OfflinePlayer offlinePlayer);

    /**
     * Get a players collection box
     *
     * @param uuid a players uuid
     * @return the collection box or null if no items found for that player
     * @since 1.0
     */
    public abstract List<CollectableItem> getCollectionBox(UUID uuid);

    /**
     * Get a players expired items
     *
     * @param offlinePlayer a player
     * @return the expired items or null if no items found for that player
     * @since 1.0
     */
    public abstract List<CollectableItem> getExpiredItems(OfflinePlayer offlinePlayer);

    /**
     * Get a players expired items
     *
     * @param uuid a players uuid
     * @return the expired items or null if no items found for that player
     * @since 1.0
     */
    public abstract List<CollectableItem> getExpiredItems(UUID uuid);

    /**
     * Get a players history
     *
     * @param offlinePlayer a player
     * @return the players history, ordered from newest to oldest
     */
    public abstract List<HistoricItem> getHistory(OfflinePlayer offlinePlayer);

    /**
     * Get a players history
     *
     * @param uuid a player uuid
     * @return the players history, ordered from newest to oldest
     */
    public abstract List<HistoricItem> getHistory(UUID uuid);

    /**
     * Get the locale for a logged action
     * @param action action
     * @return the locale
     */
    @ApiStatus.Internal
    public abstract String getLoggedActionLocale(HistoricItem.LoggedAction action);

    /**
     * Set the instance.
     * @throws IllegalStateException if the instance is already assigned
     */
    @ApiStatus.Internal
    public static void setInstance(AuctionHouseAPI newInstance) {
        if (instance != null) {
            throw new IllegalStateException("Instance has already been set");
        }
        instance = newInstance;
    }
}
