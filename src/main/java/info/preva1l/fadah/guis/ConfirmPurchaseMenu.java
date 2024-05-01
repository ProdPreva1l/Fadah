package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.filters.SortingDirection;
import info.preva1l.fadah.utils.filters.SortingMethod;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.GuiButtonType;
import info.preva1l.fadah.utils.guis.GuiHelper;
import info.preva1l.fadah.utils.helpers.TransactionLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.Instant;

public class ConfirmPurchaseMenu extends FastInv {

    @SuppressWarnings("deprecation")
    public ConfirmPurchaseMenu(Listing listing, Player player,
                               @Nullable Category category, int page,
                               @Nullable String search,
                               @Nullable SortingMethod sortingMethod,
                               @Nullable SortingDirection sortingDirection) {
        super(54, Menus.CONFIRM_TITLE.toFormattedString());

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        // Note to any developers seeing this:
        // please clean this up, im too lazy and its a heated mess
        setItem(30, GuiHelper.constructButton(GuiButtonType.CONFIRM), e -> {
            player.closeInventory();

            // Money Transfer
            Economy eco = Fadah.getINSTANCE().getEconomy();
            double price = listing.price();
            eco.withdrawPlayer(player, price);
            eco.depositPlayer(Bukkit.getOfflinePlayer(listing.owner()), price);

            // Remove Listing
            if (Fadah.getINSTANCE().getCacheSync() == null) {
                ListingCache.removeListing(listing);
            }
            CacheSync.send(listing.id(), true);
            Fadah.getINSTANCE().getDatabase().removeListing(listing.id());

            // Add to collection box
            ItemStack itemStack = listing.itemStack().clone();
            CollectableItem collectableItem = new CollectableItem(itemStack, Instant.now().toEpochMilli());
            Fadah.getINSTANCE().getDatabase().addToCollectionBox(player.getUniqueId(), collectableItem);
            CollectionBoxCache.addItem(player.getUniqueId(), collectableItem);

            // Send Cache Updates
            CacheSync.send(CacheSync.CacheType.COLLECTION_BOX, player.getUniqueId());
            CacheSync.send(CacheSync.CacheType.EXPIRED_LISTINGS, listing.owner());

            // Notify Both Players
            player.sendMessage(String.join("\n", Lang.NOTIFICATION_NEW_ITEM.toLore()));

            String itemname = listing.itemStack().getItemMeta().getDisplayName().isBlank() ? listing.itemStack().getType().name() : listing.itemStack().getItemMeta().getDisplayName();
            String formattedPrice = new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(price);
            String message = String.join("\n", Lang.NOTIFICATION_NEW_SELL.toLore(itemname, formattedPrice));

            Player seller = Bukkit.getPlayer(listing.owner());
            if (seller != null) {
                seller.sendMessage(message);
            } else {
                CacheSync.send(listing.owner(), message);
            }

            TransactionLogger.listingSold(listing, player);
        });

        setItem(32, GuiHelper.constructButton(GuiButtonType.CANCEL), e ->
                new MainMenu(category, player, page, search, sortingMethod, sortingDirection).open(player));

        setItem(22, listing.itemStack().clone());
    }
}
