package info.preva1l.fadah.records;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingEndEvent;
import info.preva1l.fadah.api.ListingEndReason;
import info.preva1l.fadah.api.ListingPurchaseEvent;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class CurrentListing extends Listing {

    public CurrentListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                          @NotNull ItemStack itemStack, @NotNull String categoryID, double price,
                          double tax, long creationDate, long deletionDate, boolean biddable, List<Bid> bids) {
        super(id, owner, ownerName, itemStack, categoryID, price, tax, creationDate, deletionDate, biddable, bids);
    }

    @Override
    public void purchase(@NotNull Player buyer) {
        if (!Fadah.getINSTANCE().getEconomy().has(buyer, this.getPrice())) {
            buyer.sendMessage(Lang.PREFIX.toFormattedString() + Lang.TOO_EXPENSIVE.toFormattedString());
            return;
        }
        if (ListingCache.getListing(this.getId()) == null
                || (Config.STRICT_CHECKS.toBoolean() && Fadah.getINSTANCE().getDatabase().getListing(this.getId()) == null)) {
            buyer.sendMessage(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString());
            return;
        }
        // Money Transfer
        Economy eco = Fadah.getINSTANCE().getEconomy();
        eco.withdrawPlayer(buyer, this.getPrice());
        double taxed = (this.getTax()/100) * this.getPrice();
        eco.depositPlayer(Bukkit.getOfflinePlayer(this.getOwner()), this.getPrice() - taxed);

        // Remove Listing
        if (Fadah.getINSTANCE().getCacheSync() == null) {
            ListingCache.removeListing(this);
        }
        CacheSync.send(this.getId(), true);
        Fadah.getINSTANCE().getDatabase().removeListing(this.getId());

        // Add to collection box
        ItemStack itemStack = this.getItemStack().clone();
        CollectableItem collectableItem = new CollectableItem(itemStack, Instant.now().toEpochMilli());
        Fadah.getINSTANCE().getDatabase().addToCollectionBox(buyer.getUniqueId(), collectableItem);
        CollectionBoxCache.addItem(buyer.getUniqueId(), collectableItem);

        // Send Cache Updates
        CacheSync.send(CacheSync.CacheType.COLLECTION_BOX, buyer.getUniqueId());
        CacheSync.send(CacheSync.CacheType.EXPIRED_LISTINGS, this.getOwner());

        // Notify Both Players
        buyer.sendMessage(String.join("\n", Lang.NOTIFICATION_NEW_ITEM.toLore()));

        String itemName = this.getItemStack().getItemMeta().getDisplayName().isBlank() ?
                this.getItemStack().getType().name() : this.getItemStack().getItemMeta().getDisplayName();
        String formattedPrice = new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(this.getPrice() - taxed);
        String message = String.join("\n", Lang.NOTIFICATION_NEW_SELL.toLore(itemName, formattedPrice));

        Player seller = Bukkit.getPlayer(this.getOwner());
        if (seller != null) {
            seller.sendMessage(message);
        } else {
            CacheSync.send(this.getOwner(), message);
        }

        TransactionLogger.listingSold(this, buyer);

        Bukkit.getServer().getPluginManager().callEvent(new ListingPurchaseEvent(this.getAsStale(), buyer));
    }

    @Override
    public boolean cancel(@NotNull Player canceller) {
        if (ListingCache.getListing(this.getId()) == null
                || (Config.STRICT_CHECKS.toBoolean()
                && Fadah.getINSTANCE().getDatabase().getListing(this.getId()) == null)) {
            canceller.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString()));
            return false;
        }
        canceller.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.CANCELLED.toFormattedString()));
        if (Fadah.getINSTANCE().getCacheSync() == null) {
            ListingCache.removeListing(this);
        }
        CacheSync.send(this.getId(), true);
        Fadah.getINSTANCE().getDatabase().removeListing(this.getId());

        CollectableItem collectableItem = new CollectableItem(this.getItemStack(), Instant.now().toEpochMilli());
        ExpiredListingsCache.addItem(getOwner(), collectableItem);
        CacheSync.send(CacheSync.CacheType.EXPIRED_LISTINGS, getOwner());

        Fadah.getINSTANCE().getDatabase().addToExpiredItems(getOwner(), collectableItem);

        boolean isAdmin = !this.isOwner(canceller);
        TransactionLogger.listingRemoval(this, isAdmin);
        Bukkit.getScheduler().runTaskAsynchronously(Fadah.getINSTANCE(), () ->
                Bukkit.getServer().getPluginManager().callEvent(
                        new ListingEndEvent(isAdmin
                                ? ListingEndReason.CANCELLED_ADMIN
                                : ListingEndReason.CANCELLED)
                )
        );
        return true;
    }

    public StaleListing getAsStale() {
        return new StaleListing(id, owner, ownerName, itemStack, categoryID, price, tax, creationDate, deletionDate, biddable, bids);
    }
}
