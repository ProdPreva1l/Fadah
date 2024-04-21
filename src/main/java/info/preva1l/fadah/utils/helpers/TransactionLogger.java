package info.preva1l.fadah.utils.helpers;

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
        HistoricItem historicItem = new HistoricItem(listing.owner(), Instant.now().toEpochMilli(), HistoricItem.LoggedAction.LISTING_START, listing.itemStack(), listing.price(), null);
        HistoricItemsCache.addLog(listing.owner(), historicItem);
        Fadah.getINSTANCE().getDatabase().addToHistory(listing.owner(), historicItem);

        // Log file logs
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[NEW LISTING] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.owner()).getName(), Bukkit.getOfflinePlayer(listing.owner()).getUniqueId().toString(),
                listing.price(), listing.itemStack().toString()));
    }
    public void listingSold(Listing listing, Player buyer) {
        // In Game logs
        HistoricItem historicItemSeller = new HistoricItem(listing.owner(), Instant.now().toEpochMilli(),
                HistoricItem.LoggedAction.LISTING_SOLD, listing.itemStack(), listing.price(), buyer.getUniqueId());

        HistoricItemsCache.addLog(listing.owner(), historicItemSeller);
        Fadah.getINSTANCE().getDatabase().addToHistory(listing.owner(), historicItemSeller);

        CacheSync.send(CacheSync.CacheType.HISTORY, listing.owner());

        HistoricItem historicItemBuyer = new HistoricItem(buyer.getUniqueId(), Instant.now().toEpochMilli(),
                HistoricItem.LoggedAction.LISTING_PURCHASED, listing.itemStack(), listing.price(), listing.owner());

        HistoricItemsCache.addLog(buyer.getUniqueId(), historicItemBuyer);
        Fadah.getINSTANCE().getDatabase().addToHistory(buyer.getUniqueId(), historicItemBuyer);

        CacheSync.send(CacheSync.CacheType.HISTORY, buyer.getUniqueId());

        // Log file logs
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[LISTING SOLD] Seller: {0} ({1}), Buyer: {2} ({3}), Price: {4}, ItemStack: {5}",
                listing.ownerName(), listing.owner(), buyer.getName(), buyer.getUniqueId(), listing.price(), listing.itemStack()));
    }
    public void listingRemoval(Listing listing, boolean isAdmin) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(listing.owner(), Instant.now().toEpochMilli(),
                isAdmin ? HistoricItem.LoggedAction.LISTING_ADMIN_CANCEL : HistoricItem.LoggedAction.LISTING_CANCEL,
                listing.itemStack(), null, null);
        HistoricItemsCache.addLog(listing.owner(), historicItem);
        Fadah.getINSTANCE().getDatabase().addToHistory(listing.owner(), historicItem);

        // Log file logs
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[LISTING REMOVED] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.owner()).getName(), Bukkit.getOfflinePlayer(listing.owner()).getUniqueId().toString(),
                listing.price(), listing.itemStack().toString()));
    }
    public void listingExpired(Listing listing) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(listing.owner(), Instant.now().toEpochMilli(), HistoricItem.LoggedAction.LISTING_EXPIRE,
                listing.itemStack(), null, null);
        HistoricItemsCache.addLog(listing.owner(), historicItem);
        Fadah.getINSTANCE().getDatabase().addToHistory(listing.owner(), historicItem);

        // Log file logs
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[LISTING EXPIRED] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.owner()).getName(), Bukkit.getOfflinePlayer(listing.owner()).getUniqueId().toString(),
                listing.price(), listing.itemStack().toString()));
    }
}
