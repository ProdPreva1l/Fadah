package info.preva1l.fadah.utils.helpers;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@UtilityClass
public class TransactionLogger {
    public void listingCreated(Listing listing) {
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[NEW LISTING] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.owner()).getName(), Bukkit.getOfflinePlayer(listing.owner()).getUniqueId().toString(),
                listing.price(), listing.itemStack().toString()));
    }
    public void listingSold(Listing listing, Player buyer) {
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[LISTING SOLD] Seller: {0} ({1}), Buyer: {2} ({3}), Price: {4}, ItemStack: {5}",
                listing.ownerName(), listing.owner(), buyer.getName(), buyer.getUniqueId(), listing.price(), listing.itemStack()));
    }
    public void listingRemoval(Listing listing) {
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[LISTING REMOVED] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.owner()).getName(), Bukkit.getOfflinePlayer(listing.owner()).getUniqueId().toString(),
                listing.price(), listing.itemStack().toString()));
    }
    public void listingExpired(Listing listing) {
        Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[LISTING EXPIRED] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.owner()).getName(), Bukkit.getOfflinePlayer(listing.owner()).getUniqueId().toString(),
                listing.price(), listing.itemStack().toString()));
    }
}
