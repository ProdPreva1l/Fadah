package info.preva1l.fadah.utils.logging;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;

@UtilityClass
public class TransactionLogger {

    public void listingCreated(Listing listing) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(listing.getOwner(), Instant.now().toEpochMilli(), HistoricItem.LoggedAction.LISTING_START, listing.getItemStack(), listing.getPrice(), null);
        HistoricItemsCache.addLog(listing.getOwner(), historicItem);
        Fadah.getINSTANCE().getDatabase().addToHistory(listing.getOwner(), historicItem);

        // Log file logs
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[NEW LISTING] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(), Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getPrice(), listing.getItemStack().toString()));
    }

    public void listingSold(Listing listing, Player buyer) {
        // In Game logs
        HistoricItem historicItemSeller = new HistoricItem(listing.getOwner(), Instant.now().toEpochMilli(),
                HistoricItem.LoggedAction.LISTING_SOLD, listing.getItemStack(), listing.getPrice(), buyer.getUniqueId());

        HistoricItemsCache.addLog(listing.getOwner(), historicItemSeller);
        Fadah.getINSTANCE().getDatabase().addToHistory(listing.getOwner(), historicItemSeller);

        CacheSync.send(CacheSync.CacheType.HISTORY, listing.getOwner());

        HistoricItem historicItemBuyer = new HistoricItem(buyer.getUniqueId(), Instant.now().toEpochMilli(),
                HistoricItem.LoggedAction.LISTING_PURCHASED, listing.getItemStack(), listing.getPrice(), listing.getOwner());

        HistoricItemsCache.addLog(buyer.getUniqueId(), historicItemBuyer);
        Fadah.getINSTANCE().getDatabase().addToHistory(buyer.getUniqueId(), historicItemBuyer);

        CacheSync.send(CacheSync.CacheType.HISTORY, buyer.getUniqueId());

        // Log file logs
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[LISTING SOLD] Seller: {0} ({1}), Buyer: {2} ({3}), Price: {4}, ItemStack: {5}",
                listing.getOwnerName(), listing.getOwner(), buyer.getName(), buyer.getUniqueId(), listing.getPrice(), listing.getItemStack()));
    }

    public void listingRemoval(Listing listing, boolean isAdmin) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(listing.getOwner(), Instant.now().toEpochMilli(),
                isAdmin ? HistoricItem.LoggedAction.LISTING_ADMIN_CANCEL : HistoricItem.LoggedAction.LISTING_CANCEL,
                listing.getItemStack(), null, null);
        HistoricItemsCache.addLog(listing.getOwner(), historicItem);
        Fadah.getINSTANCE().getDatabase().addToHistory(listing.getOwner(), historicItem);

        // Log file logs
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[LISTING REMOVED] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(), Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getPrice(), listing.getItemStack().toString()));
    }

    public void listingExpired(Listing listing) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(listing.getOwner(), Instant.now().toEpochMilli(), HistoricItem.LoggedAction.LISTING_EXPIRE,
                listing.getItemStack(), null, null);
        HistoricItemsCache.addLog(listing.getOwner(), historicItem);
        Fadah.getINSTANCE().getDatabase().addToHistory(listing.getOwner(), historicItem);

        // Log file logs
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[LISTING EXPIRED] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(), Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getPrice(), listing.getItemStack().toString()));
    }
}
