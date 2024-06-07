package info.preva1l.fadah.records;

import info.preva1l.fadah.api.AuctionHouseAPI;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A historic item
 * @param ownerUUID the person who the log belongs to
 * @param loggedDate when the action happened, in epoch millis
 * @param action the action that got logged
 * @param itemStack the item that had an action happen to it
 * @param price Nullable, only for {@link LoggedAction#LISTING_START}, {@link LoggedAction#LISTING_PURCHASED}, {@link LoggedAction#LISTING_SOLD}
 * @param purchaserUUID Nullable, only for
 */
public record HistoricItem(
        @NotNull UUID ownerUUID,
        @NotNull Long loggedDate,
        @NotNull LoggedAction action,
        @NotNull ItemStack itemStack,
        @Nullable Double price,
        @Nullable UUID purchaserUUID
) {
    public enum LoggedAction {
        LISTING_START,
        LISTING_PURCHASED,
        LISTING_SOLD,
        LISTING_CANCEL,
        LISTING_EXPIRE,
        LISTING_ADMIN_CANCEL,
        EXPIRED_ITEM_CLAIM,
        EXPIRED_ITEM_ADMIN_CLAIM,
        COLLECTION_BOX_CLAIM,
        COLLECTION_BOX_ADMIN_CLAIM;

        public String getLocaleActionName() {
            return AuctionHouseAPI.getInstance().getLoggedActionLocale(this);
        }
    }
}
