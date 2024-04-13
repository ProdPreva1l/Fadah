package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.filters.SortingDirection;
import info.preva1l.fadah.utils.filters.SortingMethod;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.GuiButtonType;
import info.preva1l.fadah.utils.guis.GuiHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class ConfirmPurchaseMenu extends FastInv {

    public ConfirmPurchaseMenu(Listing listing, Player player, @Nullable Category category, int page,
                               @Nullable String search,
                               @Nullable SortingMethod sortingMethod,
                               @Nullable SortingDirection sortingDirection) {
        super(54, Menus.CONFIRM_TITLE.toFormattedString());

        setItems(getBorders(),
                GuiHelper.constructButton(GuiButtonType.GENERIC, Material.BLACK_STAINED_GLASS_PANE,
                        StringUtils.colorize("&r "), Menus.BORDER_LORE.toLore()));


        setItem(30, GuiHelper.constructButton(GuiButtonType.CONFIRM), e->{
            player.closeInventory();

            // Money Transfer
            Fadah.getINSTANCE().getEconomy().withdrawPlayer(player, listing.price());
            Fadah.getINSTANCE().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(listing.owner()), listing.price());

            // Remove Listing
            if (Fadah.getINSTANCE().getCacheSync() == null) {
                ListingCache.removeListing(listing);
            }
            CacheSync.send(listing.id(), true);
            Fadah.getConsole().info("Removing Listing " + listing.id());
            Fadah.getINSTANCE().getDatabase().removeListing(listing.id());

            // Add to collection box
            CollectableItem collectableItem = new CollectableItem(listing.itemStack().clone(), Instant.now().toEpochMilli());
            Fadah.getINSTANCE().getDatabase().addToCollectionBox(player.getUniqueId(), collectableItem);
            CollectionBoxCache.addItem(player.getUniqueId(), collectableItem);

            // Send Cache Updates
            CacheSync.send(CacheSync.CacheType.COLLECTION_BOX, player.getUniqueId());
            CacheSync.send(CacheSync.CacheType.EXPIRED_LISTINGS, listing.owner());

            // Notify Both Players
            player.sendMessage(String.join("\n", Lang.NOTIFICATION_NEW_ITEM.toLore()));

            String itemname = listing.itemStack().getItemMeta().getDisplayName().isBlank() ? listing.itemStack().getType().name() : listing.itemStack().getItemMeta().getDisplayName();
            String message = String.join("\n", Lang.NOTIFICATION_NEW_SELL.toLore(itemname, listing.price()));

            Player seller = Bukkit.getPlayer(listing.owner());
            if (seller != null) {
                seller.sendMessage(message);
            } else {
                CacheSync.send(listing.owner(), message);
            }

            Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("ITEM SOLD Seller: {0} ({1}), Buyer: {2} ({3}), Price: {4}, ItemStack: {5}",
                    Bukkit.getOfflinePlayer(listing.owner()).getName(), Bukkit.getOfflinePlayer(listing.owner()).getUniqueId().toString(),
                    player.getName(), player.getUniqueId().toString(),
                    listing.price(), listing.itemStack().toString()));
        });

        setItem(32, GuiHelper.constructButton(GuiButtonType.CANCEL), e->{
            new MainMenu(category, player, 0, search, sortingMethod, sortingDirection).open(player);
        });

        setItem(22, listing.itemStack().clone());
    }
}
