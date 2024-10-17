package info.preva1l.fadah.records;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class StaleListing extends Listing {
    public StaleListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                         @NotNull ItemStack itemStack, @NotNull String categoryID, @NotNull String currency, double price, double tax,
                         long creationDate, long deletionDate, boolean biddable, List<Bid> bids) {
        super(id, owner, ownerName, itemStack, categoryID, currency, price, tax, creationDate, deletionDate, biddable, bids);
    }

    @Override
    public void purchase(@NotNull Player buyer) {
        throw new IllegalStateException("Purchasing a listing is not possible when the listing is in a stale state!");
    }

    @Override
    public boolean cancel(@NotNull Player canceller) {
        throw new IllegalStateException("Cancelling a listing is not possible when the listing is in a stale state!");
    }
}
