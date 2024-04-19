package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.filters.SortingDirection;
import info.preva1l.fadah.utils.filters.SortingMethod;
import info.preva1l.fadah.utils.guis.*;
import info.preva1l.fadah.utils.helpers.TransactionLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMenu extends FastInv {
    private static final int maxItemsPerPage = 24;
    private final Category category;
    private final Player player;
    private final int page;
    private final List<Listing> listings;
    private final Map<Integer, Integer> listingSlot = new HashMap<>();
    // Filters
    private final String search;
    private final SortingMethod sortingMethod;
    private final SortingDirection sortingDirection;
    private int index = 0;
    private Map<Integer, Integer> selectorMappings = new HashMap<>();

    public MainMenu(@Nullable Category category, Player player, int page,
                    @Nullable String search,
                    @Nullable SortingMethod sortingMethod,
                    @Nullable SortingDirection sortingDirection) {
        super(54, Menus.MAIN_TITLE.toFormattedString());
        this.category = category;
        this.player = player;
        this.page = page;
        this.listings = ListingCache.getListings();

        this.search = search;
        this.sortingMethod = (sortingMethod == null ? SortingMethod.AGE : sortingMethod);
        this.sortingDirection = (sortingDirection == null ? SortingDirection.ASCENDING : sortingDirection);

        Comparator<Listing> sorter = this.sortingMethod.getSorter(this.sortingDirection);
        listings.sort(sorter);

        if (category != null) {
            listings.removeIf(listing -> !listing.categoryID().equals(category.id()));
        }
        if (search != null) {
            listings.removeIf(listing -> !(listing.itemStack().getType().name().toUpperCase().contains(search.toUpperCase())
                    || listing.itemStack().getType().name().toUpperCase().contains(search.replace(" ", "_").toUpperCase()))
                    && !checkForStringInItem(search.toUpperCase(), listing.itemStack())
                    && !checkForEnchantmentOnBook(search.toUpperCase(), listing.itemStack()));
        }

        fillMappings();

        setItems(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 17, 19, 26, 28, 35, 37, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53}, GuiHelper.constructButton(GuiButtonType.BORDER));


        populateCategories();
        addNavigationButtons();
        addFilterButtons();
        // Populate Listings must always be before addPaginationControls!
        populateListings();
        addPaginationControls();
        BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(Fadah.getINSTANCE(), this::refresh, 20L, 20L);
        InventoryEventHandler.tasksToQuit.put(getInventory(), task);
    }

    private void refresh() {
        populateListings();
        addPaginationControls();
    }

    private void populateListings() {
        if (listings == null || listings.isEmpty()) {
            setItems(new int[]{22, 23, 31, 32}, new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial()).name(Menus.NO_ITEM_FOUND_NAME.toFormattedString()).lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build());
            return;
        }
        for (int i = 0; i <= maxItemsPerPage; i++) {
            index = maxItemsPerPage * page + i;
            if (index >= listings.size() || i == maxItemsPerPage) break;
            Listing listing = listings.get(index);

            ItemBuilder itemStack = new ItemBuilder(listing.itemStack().clone())
                    .addLore(Menus.MAIN_LISTING_LORE.toLore(listing.ownerName(), listing.categoryID(),
                            new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(listing.price()), TimeUtil.formatTimeUntil(listing.deletionDate())));

            if (player.getUniqueId().equals(listing.owner())) {
                itemStack.addLore(Menus.MAIN_LISTING_FOOTER_OWN_LISTING.toFormattedString());
            } else if (Fadah.getINSTANCE().getEconomy().has(player, listing.price())) {
                itemStack.addLore(Menus.MAIN_LISTING_FOOTER_BUY.toFormattedString());
            } else {
                itemStack.addLore(Menus.MAIN_LISTING_FOOTER_EXPENSIVE.toFormattedString());
            }
            if (listing.itemStack().getType().name().toUpperCase().endsWith("_SHULKER_BOX")) {
                itemStack.addLore(Menus.MAIN_LISTING_FOOTER_SHULKER.toFormattedString());
            }
            removeItem(listingSlot.get(i));
            setItem(listingSlot.get(i), itemStack.build(), e -> {
                if (handleShulkerPreview(listing, e)) {
                    return;
                }

                if (handleListingCancellation(listing, e)) {
                    return;
                }

                handlePurchase(listing);
            });
        }
    }

    private boolean handleShulkerPreview(Listing listing, InventoryClickEvent e) {
        if (listing.itemStack().getType().name().toUpperCase().endsWith("_SHULKER_BOX") && e.isRightClick()) {
            new ShulkerBoxPreviewMenu(listing, player, category, page, search, sortingMethod, sortingDirection).open(player);
            return true;
        }
        return false;
    }

    private boolean handleListingCancellation(Listing listing,  InventoryClickEvent e) {
        if (player.getUniqueId().equals(listing.owner())) {
            if (e.isShiftClick()) {
                if (ListingCache.getListing(listing.id()) == null || (Config.STRICT_CHECKS.toBoolean() && Fadah.getINSTANCE().getDatabase().getListing(listing.id()) == null)) {
                    player.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString()));
                    return true;
                }
                player.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.CANCELLED.toFormattedString()));
                if (Fadah.getINSTANCE().getCacheSync() == null) {
                    ListingCache.removeListing(listing);
                }
                CacheSync.send(listing.id(), true);
                Fadah.getINSTANCE().getDatabase().removeListing(listing.id());

                ExpiredListingsCache.addItem(player.getUniqueId(), new CollectableItem(listing.itemStack(), Instant.now().toEpochMilli()));
                CacheSync.send(CacheSync.CacheType.EXPIRED_LISTINGS, player.getUniqueId());
                new MainMenu(category, player, page, search, sortingMethod, sortingDirection).open(player);
                TransactionLogger.listingRemoval(listing);
                return true;
            }
            player.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.OWN_LISTING.toFormattedString()));
            return true;
        }
        return false;
    }

    private void handlePurchase(Listing listing) {
        if (!Fadah.getINSTANCE().getEconomy().has(player, listing.price()) || ListingCache.getListing(listing.id()) == null || (Config.STRICT_CHECKS.toBoolean() && Fadah.getINSTANCE().getDatabase().getListing(listing.id()) == null)) {
            player.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString()));
            return;
        }
        new ConfirmPurchaseMenu(listing, player, category, page, search, sortingMethod, sortingDirection).open(player);
    }

    private void populateCategories() {
        int i = 0;
        for (Category cat : CategoryCache.getCategories()) {
            ItemBuilder itemBuilder = new ItemBuilder(cat.icon())
                    .name(StringUtils.colorize(cat.name()))
                    .addLore(StringUtils.colorizeList(cat.description()))
                    .flags(ItemFlag.HIDE_ENCHANTS);
            if (category == cat) {
                itemBuilder.enchant(Enchantment.DURABILITY);
            }
            if (!selectorMappings.containsKey(i)) continue;
            int slot = selectorMappings.get(i);
            removeItem(slot);
            setItem(slot, itemBuilder.build(), e -> {
                if (category != cat) {
                    new MainMenu(cat, player, 0, search, sortingMethod, sortingDirection).open(player);
                    return;
                }
                new MainMenu(null, player, 0, search, sortingMethod, sortingDirection).open(player);
            });
            i++;
        }
    }

    private void addPaginationControls() {
        if (page > 0) {
            setItem(48, GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> new MainMenu(category, player, page - 1, search, sortingMethod, sortingDirection).open(player));
        }

        if (listings != null && listings.size() >= index + 1) {
            setItem(50, GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> new MainMenu(category, player, page + 1, search, sortingMethod, sortingDirection).open(player));
        }

    }

    private void addNavigationButtons() {
        setItem(0, GuiHelper.constructButton(GuiButtonType.SCROLL_PREVIOUS), e -> {
            shiftMappingsDOWN();
            populateCategories();
        });
        setItem(45, GuiHelper.constructButton(GuiButtonType.SCROLL_NEXT), e -> {
            shiftMappingsUP();
            populateCategories();
        });

        setItem(53, new ItemBuilder(Material.PLAYER_HEAD).skullOwner(player).name(Menus.MAIN_PROFILE_NAME.toFormattedString())
                .addLore(Menus.MAIN_PROFILE_LORE.toLore()).build(), e -> {
            new ProfileMenu(player).open(player);
        });

    }

    private void addFilterButtons() {
        // Filter Type Cycle
        setItem(47, new ItemBuilder(Menus.MAIN_FILTER_TYPE_ICON.toMaterial()).name(Menus.MAIN_FILTER_TYPE_NAME.toFormattedString())
                .addLore(Menus.MAIN_FILTER_TYPE_LORE.toLore((sortingMethod.previous() == null ? "None" : sortingMethod.previous().getFriendlyName()),
                        sortingMethod.getFriendlyName(), (sortingMethod.next() == null ? "None" : sortingMethod.next().getFriendlyName()))).build(), e -> {
            if (e.isLeftClick()) {
                if (sortingMethod.previous() == null) return;
                new MainMenu(category, player, 0, search, sortingMethod.previous(), sortingDirection).open(player);
            }
            if (e.isRightClick()) {
                if (sortingMethod.next() == null) return;
                new MainMenu(category, player, 0, search, sortingMethod.next(), sortingDirection).open(player);
            }
        });

        // Search
        setItem(49, new ItemBuilder(Menus.MAIN_SEARCH_ICON.toMaterial()).name(Menus.MAIN_SEARCH_NAME.toFormattedString())
                .lore(Menus.MAIN_SEARCH_LORE.toLore()).build(), e ->
                new SearchMenu(player, search -> new MainMenu(category, player, page, search, sortingMethod, sortingDirection).open(player)));

        // Filter Direction Toggle
        String l1 = StringUtils.formatPlaceholders(sortingDirection == SortingDirection.ASCENDING ? Menus.MAIN_FILTER_DIRECTION_SELECTED.toString() : Menus.MAIN_FILTER_DIRECTION_NOT_SELECTED.toString(),
                (sortingMethod == SortingMethod.AGE ? SortingDirection.ASCENDING.getAgeName() : SortingDirection.ASCENDING.getFriendlyName()));
        String l2 = StringUtils.formatPlaceholders(sortingDirection == SortingDirection.DESCENDING ? Menus.MAIN_FILTER_DIRECTION_SELECTED.toString() : Menus.MAIN_FILTER_DIRECTION_NOT_SELECTED.toString(),
                (sortingMethod == SortingMethod.AGE ? SortingDirection.DESCENDING.getAgeName() : SortingDirection.DESCENDING.getFriendlyName()));

        setItem(51, new ItemBuilder(Menus.MAIN_FILTER_DIRECTION_ICON.toMaterial()).name(Menus.MAIN_FILTER_DIRECTION_NAME.toFormattedString()).lore(Menus.MAIN_FILTER_DIRECTION_LORE.toLore(l1, l2)).build(), e ->
                new MainMenu(category, player, 0, search, sortingMethod,
                        (sortingDirection == SortingDirection.ASCENDING ? SortingDirection.DESCENDING : SortingDirection.ASCENDING)).open(player));
    }

    private void shiftMappingsUP() {
        if (selectorMappings.containsKey(CategoryCache.getCategories().size() - 1)) return;
        Map<Integer, Integer> newMappings = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : selectorMappings.entrySet()) {
            newMappings.put(entry.getKey() + 1, entry.getValue());
        }
        selectorMappings = newMappings;
    }

    private void shiftMappingsDOWN() {
        if (selectorMappings.containsKey(0)) return;
        Map<Integer, Integer> newMappings = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : selectorMappings.entrySet()) {
            newMappings.put(entry.getKey() - 1, entry.getValue());
        }
        selectorMappings = newMappings;
    }

    private boolean checkForEnchantmentOnBook(String enchant, ItemStack enchantedBook) {
        if (enchantedBook.getType() == Material.ENCHANTED_BOOK) {
            for (Enchantment enchantment : enchantedBook.getEnchantments().keySet()) {
                if (enchantment.getKey().getKey().toUpperCase().contains(enchant)) return true;
            }
        }
        return false;
    }

    private boolean checkForStringInItem(String toCheck, ItemStack item) {
        if (item.hasItemMeta()) {
            return item.getItemMeta().getDisplayName().toUpperCase().contains(toCheck.toUpperCase()) || (item.getItemMeta().getLore() != null && item.getItemMeta().getLore().contains(toCheck.toUpperCase()));
        }
        return false;
    }

    private void fillMappings() {
        selectorMappings.put(0, 9);
        selectorMappings.put(1, 18);
        selectorMappings.put(2, 27);
        selectorMappings.put(3, 36);

        listingSlot.put(0, 11);
        listingSlot.put(1, 12);
        listingSlot.put(2, 13);
        listingSlot.put(3, 14);
        listingSlot.put(4, 15);
        listingSlot.put(5, 16);
        listingSlot.put(6, 20);
        listingSlot.put(7, 21);
        listingSlot.put(8, 22);
        listingSlot.put(9, 23);
        listingSlot.put(10, 24);
        listingSlot.put(11, 25);
        listingSlot.put(12, 29);
        listingSlot.put(13, 30);
        listingSlot.put(14, 31);
        listingSlot.put(15, 32);
        listingSlot.put(16, 33);
        listingSlot.put(17, 34);
        listingSlot.put(18, 38);
        listingSlot.put(19, 39);
        listingSlot.put(20, 40);
        listingSlot.put(21, 41);
        listingSlot.put(22, 42);
        listingSlot.put(23, 43);
    }
}
