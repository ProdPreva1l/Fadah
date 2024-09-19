package info.preva1l.fadah.guis;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingCreateEvent;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.hooks.impl.DiscordHook;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.multiserver.Payload;
import info.preva1l.fadah.records.CurrentListing;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import net.milkbowl.vault.economy.Economy;
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
    private boolean advertise = Config.i().getListingAdverts().isEnabledByDefault();
    private boolean isBidding = false;

    public NewListingMenu(Player player, double price) {
        super(LayoutManager.MenuType.NEW_LISTING.getLayout().guiSize(),
                LayoutManager.MenuType.NEW_LISTING.getLayout().guiTitle(), LayoutManager.MenuType.NEW_LISTING);
        this.player = player;
        this.itemToSell = player.getInventory().getItemInMainHand().clone();
        this.timeToDelete = Instant.now().plus(2, ChronoUnit.DAYS);
        List<Integer> fillerSlots = getLayout().fillerSlots();
        if (!fillerSlots.isEmpty()) {
            setItems(fillerSlots.stream().mapToInt(Integer::intValue).toArray(),
                    GuiHelper.constructButton(GuiButtonType.BORDER));
        }

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_START, -1),
                new ItemBuilder(getLang().getAsMaterial("create.icon", Material.EMERALD))
                        .name(getLang().getStringFormatted("create.name", "&aClick to create listing!"))
                        .modelData(getLang().getInt("create.model-data"))
                        .addLore(getLang().getLore("create.lore",
                                new DecimalFormat(Config.i().getDecimalFormat())
                                        .format(price)))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .build(), e -> startListing(timeToDelete, price));
        setClock();
        setAdvertButton();
        //setModeButton();
        addNavigationButtons();

        MultiLib.getEntityScheduler(player).execute(plugin,
                () -> player.getInventory().setItemInMainHand(new ItemStack(Material.AIR)),
                () -> this.itemToSell = new ItemStack(Material.AIR),
                0L);
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_ITEM, -1), itemToSell);
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {
        super.onClose(event);
        if (!listingStarted) player.getInventory().setItemInMainHand(itemToSell);
    }

    private void setClock() {
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_TIME, -1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_TIME, -1),
                new ItemBuilder(getLang().getAsMaterial("time.icon", Material.CLOCK))
                        .name(getLang().getStringFormatted("time.name", "&aTime for listing to be active"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(getLang().getLore("time.lore", TimeUtil.formatTimeUntil(timeToDelete.toEpochMilli()))).build(), e -> {
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

    private void setAdvertButton() {
        String postAdvert = StringUtils.formatPlaceholders(advertise
                        ? getLang().getStringFormatted("advert.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("advert.options.not-selected", "&f{0}"),
                Lang.ADVERT_POST.toFormattedString());
        String dontPost = StringUtils.formatPlaceholders(!advertise
                        ? getLang().getStringFormatted("advert.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("advert.options.not-selected", "&f{0}"),
                Lang.ADVERT_DONT_POST.toFormattedString());

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_ADVERT, -1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_ADVERT, -1),
                new ItemBuilder(getLang().getAsMaterial("advert.icon", Material.OAK_SIGN))
                        .name(getLang().getStringFormatted("advert.name", "&eAdvertise Listing"))
                        .modelData(getLang().getInt("advert.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(getLang().getLore("advert.lore",
                                new DecimalFormat(Config.i().getDecimalFormat())
                                        .format(PermissionsData.getHighestDouble(PermissionsData.PermissionType.ADVERT_PRICE, player)),
                                postAdvert, dontPost)).build(), e -> {
                    this.advertise = !advertise;
                    setAdvertButton();
                }
        );
    }
    // Not Used (For future bidding update)
    private void setModeButton() {
        String bidding = StringUtils.formatPlaceholders(isBidding
                        ? getLang().getStringFormatted("mode.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("mode.options.not-selected", "&f{0}"),
                Lang.MODE_BIDDING.toFormattedString());
        String bin = StringUtils.formatPlaceholders(!isBidding
                        ? getLang().getStringFormatted("mode.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("mode.options.not-selected", "&f{0}"),
                Lang.MODE_BUY_IT_NOW.toFormattedString());

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_MODE, -1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_MODE, -1),
                new ItemBuilder(getLang().getAsMaterial("mode.icon", Material.HOPPER))
                        .name(getLang().getStringFormatted("mode.name", "&bAuction Mode"))
                        .modelData(getLang().getInt("mode.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(getLang().getLore("mode.lore", bidding, bin)).build(), e -> {
                    this.isBidding = !isBidding;
                    setModeButton();
                }
        );
    }

    private void startListing(Instant deletionDate, double price) {
        String category = CategoryCache.getCategoryForItem(itemToSell);

        if (category == null) {
            player.sendMessage(Lang.CANT_SELL.toFormattedString());
            return;
        }

        double tax = PermissionsData.getHighestDouble(PermissionsData.PermissionType.LISTING_TAX, player);

        Listing listing = new CurrentListing(UUID.randomUUID(), player.getUniqueId(), player.getName(),
                itemToSell, category, price, tax, Instant.now().toEpochMilli(), deletionDate.toEpochMilli(), isBidding, Collections.emptyList());

        ListingCreateEvent createEvent = new ListingCreateEvent(player, listing);
        Bukkit.getServer().getPluginManager().callEvent(createEvent);

        if (createEvent.isCancelled()) {
            Lang.sendMessage(player, Lang.i().getPrefix() + createEvent.getCancelReason());
            player.closeInventory();
            return;
        }

        DatabaseManager.getInstance().save(Listing.class, listing);

        ListingCache.addListing(listing);
        Message.builder()
                .type(Message.Type.LISTING_ADD)
                .payload(Payload.withUUID(listing.getId()))
                .build().send(Fadah.getINSTANCE().getBroker());

        listingStarted = true;

        player.closeInventory();

        double taxAmount = PermissionsData.getHighestDouble(PermissionsData.PermissionType.LISTING_TAX, player);
        String itemName = StringUtils.extractItemName(listing.getItemStack());
        String message = String.join("\n", Lang.NOTIFICATION_NEW_LISTING.toLore(itemName,
                new DecimalFormat(Config.i().getDecimalFormat()).format(listing.getPrice()),
                TimeUtil.formatTimeUntil(listing.getDeletionDate()), PermissionsData.getCurrentListings(player),
                PermissionsData.getHighestInt(PermissionsData.PermissionType.MAX_LISTINGS, player),
                taxAmount, new DecimalFormat(Config.i().getDecimalFormat()).format((taxAmount / 100) * price)));
        player.sendMessage(message);

        TransactionLogger.listingCreated(listing);

        Config.Hooks.Discord discConf = Config.i().getHooks().getDiscord();
        if ((discConf.isEnabled() && plugin.getHookManager().getHook(DiscordHook.class).isPresent()) &&
                ((discConf.isEnabled() && advertise)
                        || !discConf.isOnlySendOnAdvert())) {
            plugin.getHookManager().getHook(DiscordHook.class).get().send(listing);
        }

        if (advertise) {
            Economy eco = Fadah.getINSTANCE().getEconomy();
            double advertPrice = PermissionsData.getHighestDouble(PermissionsData.PermissionType.ADVERT_PRICE, player);
            if (!eco.has(player, advertPrice)) {
                player.sendMessage(Lang.PREFIX.toFormattedString() + Lang.ADVERT_EXPENSE.toFormattedString());
                return;
            }

            eco.withdrawPlayer(player, advertPrice);

            String advertMessage = String.join("&r\n", Lang.NOTIFICATION_ADVERT.toStringList(
                    player.getName(), itemName,
                    new DecimalFormat(Config.i().getDecimalFormat()).format(listing.getPrice())));

            Message.builder()
                    .type(Message.Type.BROADCAST)
                    .payload(Payload.withBroadcast(advertMessage, "/ah view-listing " + listing.getId()))
                    .build().send(Fadah.getINSTANCE().getBroker());
        }
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.CLOSE, 49),
                GuiHelper.constructButton(GuiButtonType.CLOSE), e -> e.getWhoClicked().closeInventory());
    }
}
