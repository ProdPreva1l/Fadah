package info.preva1l.fadah.guis;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingCreateEvent;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.CurrentListing;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NewListingMenu extends FastInv {
    private final Fadah plugin = Fadah.getINSTANCE();
    private final Player player;
    private ItemStack itemToSell;
    private Instant timeToDelete;
    private boolean listingStarted = false;
    private boolean isBidding = false;

    public NewListingMenu(Player player, double price) {
        super(LayoutManager.MenuType.NEW_LISTING.getLayout().guiSize(), LayoutManager.MenuType.NEW_LISTING.getLayout().guiTitle(), LayoutManager.MenuType.NEW_LISTING);
        this.player = player;
        this.itemToSell = player.getInventory().getItemInMainHand().clone();
        this.timeToDelete = Instant.now().plus(6, ChronoUnit.HOURS);
        List<Integer> fillerSlots = getLayout().fillerSlots();
        if (!fillerSlots.isEmpty()) {
            setItems(fillerSlots.stream().mapToInt(Integer::intValue).toArray(),
                    GuiHelper.constructButton(GuiButtonType.BORDER));
        }

        List<String> createDefLore = List.of(
                "&cClicking this button will immediately post",
                "&cyour item on the auction house for &a${0}");
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_START, 30),
                new ItemBuilder(getLang().getAsMaterial("create.icon", Material.EMERALD))
                        .name(getLang().getStringFormatted("create.name", "&aClick to create listing!"))
                        .modelData(getLang().getInt("create.model-data"))
                        .addLore(getLang().getLore("create.lore", createDefLore,
                                new DecimalFormat(Config.DECIMAL_FORMAT.toString())
                                        .format(price)))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .build(), e -> startListing(timeToDelete, price));
        setClock();
        //setModeButton();
        addNavigationButtons();

        MultiLib.getEntityScheduler(player).execute(plugin,
                () -> player.getInventory().setItemInMainHand(new ItemStack(Material.AIR)),
                () -> this.itemToSell = new ItemStack(Material.AIR),
                0L);
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_ITEM, 22), itemToSell);
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {
        super.onClose(event);
        if (!listingStarted) player.getInventory().setItemInMainHand(itemToSell);
    }

    private void setClock() {
        List<String> timeDefLore = List.of(
                "&fCurrent: &6{0}",
                "&7Left Click to Add 1 Hour",
                "&7Right Click to Remove 1 Hour",
                "&7Shift Left Click to Add 30 Minutes",
                "&7Shift Right Click to Remove 30 Minutes"
        );
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_TIME, 32));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_TIME, 32),
                new ItemBuilder(getLang().getAsMaterial("time.icon", Material.CLOCK))
                        .name(getLang().getStringFormatted("time.name", "&aTime for listing to be active"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(getLang().getLore("time.lore", timeDefLore, TimeUtil.formatTimeUntil(timeToDelete.toEpochMilli()))).build(), e -> {
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

    // Not Used (For future bidding update)
    private void setModeButton() {
        List<String> defModeLore = List.of(
                "&7Click To Toggle",
                "&8-------------------------",
                "{0}",
                "{1}",
                "&8-------------------------"
        );
        String bidding = StringUtils.formatPlaceholders(isBidding
                        ? getLang().getStringFormatted("mode.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("mode.options.not-selected", "&f{0}"),
                Lang.MODE_BIDDING.toFormattedString());
        String bin = StringUtils.formatPlaceholders(!isBidding
                        ? getLang().getStringFormatted("mode.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("mode.options.not-selected", "&f{0}"),
                Lang.MODE_BUY_IT_NOW.toFormattedString());

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_MODE,31));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_MODE,31),
                new ItemBuilder(getLang().getAsMaterial("mode.icon", Material.HOPPER))
                        .name(getLang().getStringFormatted("mode.name", "&bAuction Mode"))
                        .modelData(getLang().getInt("mode.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(getLang().getLore("mode.lore", defModeLore, bidding, bin)).build(), e -> {
                    this.isBidding = !isBidding;
                    setModeButton();
                }
        );
    }

    private void startListing(Instant deletionDate, double price) {
        ListingCreateEvent createEvent = new ListingCreateEvent();
        Bukkit.getServer().getPluginManager().callEvent(createEvent);
        if (createEvent.isCancelled()) {
            player.sendMessage(Lang.PREFIX.toFormattedString() + StringUtils.message(createEvent.getCancelReason()));
            return;
        }

        String category = CategoryCache.getCategoryForItem(itemToSell);

        if (category == null) {
            player.sendMessage(Lang.CANT_SELL.toFormattedString());
            return;
        }

        double tax = PermissionsData.getHighestDouble(PermissionsData.PermissionType.LISTING_TAX, player);

        Listing listing = new CurrentListing(UUID.randomUUID(), player.getUniqueId(), player.getName(),
                itemToSell, category, price, tax, Instant.now().toEpochMilli(), deletionDate.toEpochMilli(), isBidding, Collections.emptyList());

        plugin.getDatabase().addListing(listing);
        if (plugin.getCacheSync() != null) {
            CacheSync.send(listing.getId(), false);
        } else {
            ListingCache.addListing(listing);
        }

        listingStarted = true;

        player.closeInventory();

        double taxAmount = PermissionsData.getHighestDouble(PermissionsData.PermissionType.LISTING_TAX, player);
        String itemname = listing.getItemStack().getItemMeta().getDisplayName().isBlank() ? listing.getItemStack().getType().name() : listing.getItemStack().getItemMeta().getDisplayName();
        String message = String.join("\n", Lang.NOTIFICATION_NEW_LISTING.toLore(itemname,
                new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(listing.getPrice()),
                TimeUtil.formatTimeUntil(listing.getDeletionDate()), PermissionsData.getCurrentListings(player),
                PermissionsData.getHighestInt(PermissionsData.PermissionType.MAX_LISTINGS, player),
                taxAmount, (taxAmount/100) * price));
        player.sendMessage(message);

        TransactionLogger.listingCreated(listing);
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.CLOSE, 49),
                GuiHelper.constructButton(GuiButtonType.CLOSE), e -> e.getWhoClicked().closeInventory());
    }
}
