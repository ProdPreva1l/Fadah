package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.GuiButtonType;
import info.preva1l.fadah.utils.guis.GuiHelper;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.helpers.TransactionLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class NewListingMenu extends FastInv {
    private final Player player;
    private final ItemStack itemToSell;
    private Instant timeToDelete;
    private boolean listingStarted = false;

    public NewListingMenu(Player player, double price) {
        super(54, Menus.NEW_LISTING_TITLE.toFormattedString());
        this.player = player;
        this.itemToSell = player.getInventory().getItemInMainHand().clone();

        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

        timeToDelete = Instant.now().plus(6, ChronoUnit.HOURS);

        setClock();

        setItem(30, new ItemBuilder(Menus.NEW_LISTING_CREATE_ICON.toMaterial()).name(Menus.NEW_LISTING_CREATE_NAME.toFormattedString())
                .addLore(Menus.NEW_LISTING_CREATE_LORE.toLore(new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(price))).build(), e -> startListing(timeToDelete, price));

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        addNavigationButtons();

        setItem(22, itemToSell);
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {
        super.onClose(event);
        if (!listingStarted) player.getInventory().setItemInMainHand(itemToSell);
    }

    private void setClock() {
        removeItem(32);
        setItem(32, new ItemBuilder(Menus.NEW_LISTING_TIME_ICON.toMaterial()).name(Menus.NEW_LISTING_TIME_NAME.toFormattedString())
                .addLore(Menus.NEW_LISTING_TIME_LORE.toLore(TimeUtil.formatTimeUntil(timeToDelete.toEpochMilli()))).build(), e -> {
            if (e.isRightClick()) {
                if (e.isShiftClick()) {
                    if (timeToDelete.minus(30, ChronoUnit.MINUTES).toEpochMilli() <= Instant.now().toEpochMilli())
                        return;
                    timeToDelete = timeToDelete.minus(30, ChronoUnit.MINUTES);
                    setClock();
                    return;
                }
                if (timeToDelete.minus(1, ChronoUnit.HOURS).toEpochMilli() <= Instant.now().toEpochMilli()) return;
                timeToDelete = timeToDelete.minus(1, ChronoUnit.HOURS);
                setClock();
            }

            if (e.isLeftClick()) {
                if (e.isShiftClick()) {
                    if (timeToDelete.plus(30, ChronoUnit.MINUTES).toEpochMilli() > Instant.now().plus(10, ChronoUnit.DAYS).toEpochMilli())
                        return;
                    timeToDelete = timeToDelete.plus(30, ChronoUnit.MINUTES);
                    setClock();
                    return;
                }
                if (timeToDelete.plus(1, ChronoUnit.HOURS).toEpochMilli() > Instant.now().plus(10, ChronoUnit.DAYS).toEpochMilli())
                    return;
                timeToDelete = timeToDelete.plus(1, ChronoUnit.HOURS);
                setClock();
            }
        });
    }

    //TODO: Double check this works
    private void startListing(Instant deletionDate, double price) {
        boolean isCustomItem = itemToSell.hasItemMeta() && itemToSell.getItemMeta().getPersistentDataContainer().has(Fadah.getCustomItemKey());
        String category = CategoryCache.getCategoryForItem(itemToSell, isCustomItem);

        Listing listing = new Listing(UUID.randomUUID(), player.getUniqueId(), player.getName(),
                itemToSell, category, price, Instant.now().toEpochMilli(), deletionDate.toEpochMilli());

        Fadah.getINSTANCE().getDatabase().addListing(listing);
        if (Fadah.getINSTANCE().getCacheSync() != null) {
            CacheSync.send(listing.id(), false);
        } else {
            ListingCache.addListing(listing);
        }

        listingStarted = true;

        player.closeInventory();

        String itemname = listing.itemStack().getItemMeta().getDisplayName().isBlank() ? listing.itemStack().getType().name() : listing.itemStack().getItemMeta().getDisplayName();
        String message = String.join("\n", Lang.NOTIFICATION_NEW_LISTING.toLore(itemname,
                new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(listing.price()), TimeUtil.formatTimeUntil(listing.deletionDate()), PermissionsData.getCurrentListings(player), PermissionsData.valueFromPermission(PermissionsData.PermissionType.MAX_LISTINGS, player)));
        player.sendMessage(message);

        TransactionLogger.listingCreated(listing);
    }

    private void addNavigationButtons() {
        setItem(49, GuiHelper.constructButton(GuiButtonType.CLOSE), e -> e.getWhoClicked().closeInventory());
    }
}
