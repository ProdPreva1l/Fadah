package info.preva1l.fadah.records;

import com.google.gson.annotations.Expose;
import info.preva1l.fadah.api.AuctionHouseAPI;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Getter
public class HistoricItem {
    private final @Expose @NotNull UUID ownerUUID;
    private final @Expose @NotNull Long loggedDate;
    private final @Expose @NotNull LoggedAction action;
    private final @Expose @NotNull ItemStack itemStack;
    private final @Expose @Nullable Double price;
    private final @Expose @Nullable UUID purchaserUUID;

    /**
     * A historic item
     * @param ownerUUID the person who the log belongs to
     * @param loggedDate when the action happened, in epoch millis
     * @param action the action that got logged
     * @param itemStack the item that had an action happen to it
     * @param price Nullable, only used for {@link LoggedAction#LISTING_START}, {@link LoggedAction#LISTING_PURCHASED}, {@link LoggedAction#LISTING_SOLD}
     * @param purchaserUUID Nullable, only used for {@link LoggedAction#LISTING_SOLD}
     */
    public HistoricItem(@NotNull UUID ownerUUID, @NotNull Long loggedDate, @NotNull LoggedAction action,
                        @NotNull ItemStack itemStack, @Nullable Double price, @Nullable UUID purchaserUUID) {
        this.ownerUUID = ownerUUID;
        this.loggedDate = loggedDate;
        this.action = action;
        this.itemStack = itemStack;
        this.price = price;
        this.purchaserUUID = purchaserUUID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricItem that = (HistoricItem) o;
        return Objects.equals(price, that.price) && Objects.equals(ownerUUID, that.ownerUUID) && Objects.equals(loggedDate, that.loggedDate) && Objects.equals(purchaserUUID, that.purchaserUUID) && action == that.action && Objects.equals(itemStack, that.itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerUUID, loggedDate, action, itemStack, price, purchaserUUID);
    }

    /**
     * The order must not change as ordinals are used.
     */
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
