package info.preva1l.fadah.records;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.helpers.TransactionLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.UUID;

public record Listing(
        @NotNull UUID id,
        @NotNull UUID owner,
        @NotNull String ownerName,
        @NotNull ItemStack itemStack,
        @NotNull String categoryID,
        double price,
        long creationDate,
        long deletionDate
) {
    public boolean isOwner(@NotNull Player player) {
        return player.getUniqueId().equals(this.owner);
    }

    public boolean isOwner(@NotNull UUID uuid) {
        return this.owner.equals(uuid);
    }

    public void purchase(@NotNull Player buyer) {
        // Money Transfer
        Economy eco = Fadah.getINSTANCE().getEconomy();
        eco.withdrawPlayer(buyer, this.price);
        eco.depositPlayer(Bukkit.getOfflinePlayer(this.owner), this.price);

        // Remove Listing
        if (Fadah.getINSTANCE().getCacheSync() == null) {
            ListingCache.removeListing(this);
        }
        CacheSync.send(this.id, true);
        Fadah.getINSTANCE().getDatabase().removeListing(this.id);

        // Add to collection box
        ItemStack itemStack = this.itemStack.clone();
        CollectableItem collectableItem = new CollectableItem(itemStack, Instant.now().toEpochMilli());
        Fadah.getINSTANCE().getDatabase().addToCollectionBox(buyer.getUniqueId(), collectableItem);
        CollectionBoxCache.addItem(buyer.getUniqueId(), collectableItem);

        // Send Cache Updates
        CacheSync.send(CacheSync.CacheType.COLLECTION_BOX, buyer.getUniqueId());
        CacheSync.send(CacheSync.CacheType.EXPIRED_LISTINGS, this.owner);

        // Notify Both Players
        buyer.sendMessage(String.join("\n", Lang.NOTIFICATION_NEW_ITEM.toLore()));

        String itemName = this.itemStack.getItemMeta().getDisplayName().isBlank() ?
                this.itemStack.getType().name() : this.itemStack.getItemMeta().getDisplayName();
        String formattedPrice = new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(price);
        String message = String.join("\n", Lang.NOTIFICATION_NEW_SELL.toLore(itemName, formattedPrice));

        Player seller = Bukkit.getPlayer(this.owner);
        if (seller != null) {
            seller.sendMessage(message);
        } else {
            CacheSync.send(this.owner, message);
        }

        TransactionLogger.listingSold(this, buyer);
    }

    public void cancel(@NotNull Player canceller) {
        if (!this.isOwner(canceller)) {
            return;
        }

        if (ListingCache.getListing(this.id()) == null || (Config.STRICT_CHECKS.toBoolean() && Fadah.getINSTANCE().getDatabase().getListing(this.id()) == null)) {
            canceller.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString()));
            return;
        }
        canceller.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.CANCELLED.toFormattedString()));
        if (Fadah.getINSTANCE().getCacheSync() == null) {
            ListingCache.removeListing(this);
        }
        CacheSync.send(this.id(), true);
        Fadah.getINSTANCE().getDatabase().removeListing(this.id());

        CollectableItem collectableItem = new CollectableItem(this.itemStack(), Instant.now().toEpochMilli());
        ExpiredListingsCache.addItem(owner, collectableItem);
        CacheSync.send(CacheSync.CacheType.EXPIRED_LISTINGS, owner);

        Fadah.getINSTANCE().getDatabase().addToExpiredItems(owner, collectableItem);
        TransactionLogger.listingRemoval(this, false);
    }
}