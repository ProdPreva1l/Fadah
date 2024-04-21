package info.preva1l.fadah.records;

import info.preva1l.fadah.config.Lang;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
    @Getter
    @AllArgsConstructor
    public enum LoggedAction {
        LISTING_START(Lang.ACTIONS_LISTING_START.toFormattedString()),
        LISTING_PURCHASED(Lang.ACTIONS_LISTING_PURCHASED.toFormattedString()),
        LISTING_SOLD(Lang.ACTIONS_LISTING_SOLD.toFormattedString()),
        LISTING_CANCEL(Lang.ACTIONS_LISTING_CANCEL.toFormattedString()),
        LISTING_EXPIRE(Lang.ACTIONS_LISTING_EXPIRE.toFormattedString()),
        LISTING_ADMIN_CANCEL(Lang.ACTIONS_LISTING_ADMIN_CANCEL.toFormattedString()),
        EXPIRED_ITEM_CLAIM(Lang.ACTIONS_EXPIRED_ITEM_CLAIM.toFormattedString()),
        EXPIRED_ITEM_ADMIN_CLAIM(Lang.ACTIONS_EXPIRED_ITEM_ADMIN_CLAIM.toFormattedString()),
        COLLECTION_BOX_CLAIM(Lang.ACTIONS_COLLECTION_BOX_CLAIM.toFormattedString()),
        COLLECTION_BOX_ADMIN_CLAIM(Lang.ACTIONS_COLLECTION_BOX_ADMIN_CLAIM.toFormattedString());
        private final String localeActionName;
    }
}
