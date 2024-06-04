package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.filters.SortingDirection;
import info.preva1l.fadah.utils.filters.SortingMethod;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMenu extends PaginatedFastInv implements ScrollBar {
    private Category category;
    private final List<Listing> listings;

    // Filters
    private final String search;
    private SortingMethod sortingMethod;
    private SortingDirection sortingDirection;

    public MainMenu(@Nullable Category category, @NotNull Player player, @Nullable String search,
                    @Nullable SortingMethod sortingMethod, @Nullable SortingDirection sortingDirection) {
        super(54, Menus.MAIN_TITLE.toFormattedString(), player);
        this.category = category;
        this.listings = ListingCache.getListings();

        this.search = search;
        this.sortingMethod = (sortingMethod == null ? SortingMethod.AGE : sortingMethod);
        this.sortingDirection = (sortingDirection == null ? SortingDirection.ASCENDING : sortingDirection);

        listings.sort(this.sortingMethod.getSorter(this.sortingDirection));

        if (category != null) {
            listings.removeIf(listing -> !listing.categoryID().equals(category.id()));
        }
        if (search != null) {
            listings.removeIf(listing -> !(listing.itemStack().getType().name().toUpperCase().contains(search.toUpperCase())
                    || listing.itemStack().getType().name().toUpperCase().contains(search.replace(" ", "_").toUpperCase()))
                    && !checkForStringInItem(search.toUpperCase(), listing.itemStack())
                    && !checkForEnchantmentOnBook(search.toUpperCase(), listing.itemStack()));
        }

        setItems(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 17, 19, 26, 28, 35, 37, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53},
                GuiHelper.constructButton(GuiButtonType.BORDER));

        addNavigationButtons();
        addFilterButtons();

        fillScrollbarItems();
        fillPaginationItems();

        populateScrollbar();

        // Populate Listings must always be before addPaginationControls!
        populatePage();
        addPaginationControls();
    }

    private boolean checkForEnchantmentOnBook(String enchant, ItemStack enchantedBook) {
        if (enchantedBook.getType() == Material.ENCHANTED_BOOK) {
            for (Enchantment enchantment : enchantedBook.getEnchantments().keySet()) {
                if (enchantment.getKey().getKey().toUpperCase().contains(enchant)) return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean checkForStringInItem(String toCheck, ItemStack item) {
        if (item.hasItemMeta()) {
            return item.getItemMeta().getDisplayName().toUpperCase().contains(toCheck.toUpperCase())
                    || (item.getItemMeta().getLore() != null
                    && item.getItemMeta().getLore().contains(toCheck.toUpperCase()));
        }
        return false;
    }

    // When making the gui layout editor use this method to change where the scrollbar slots are
    @Override
    public Map<Integer, Integer> scrollbarSlots() {
        Map<Integer, Integer> temp = new HashMap<>();
        temp.put(0, 9);
        temp.put(1, 18);
        temp.put(2, 27);
        temp.put(3, 36);
        return temp;
    }

    @Override
    public void fillScrollbarItems() {
        for (Category cat : CategoryCache.getCategories()) {
            ItemBuilder itemBuilder = new ItemBuilder(cat.icon())
                    .name(StringUtils.colorize(cat.name()))
                    .addLore(StringUtils.colorizeList(cat.description()))
                    .flags(ItemFlag.HIDE_ENCHANTS);
            if (category == cat) {
                itemBuilder.name(StringUtils.colorize(cat.name() + "&r " + Lang.CATEGORY_SELECTED.toFormattedString()))
                        .enchant(Enchantment.DURABILITY);
            }

            addScrollbarItem(new PaginatedItem(itemBuilder.build(), e -> {
                if (category != cat) {
                    this.category = cat;
                    populateScrollbar();
                }
            }));
        }
    }

    @Override
    protected void fillPaginationItems() {
        for (Listing listing : listings) {
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

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                if (e.isShiftClick()) {
                    listing.cancel(((Player) e.getWhoClicked()));
                    return;
                }

                if (e.isRightClick() && listing.itemStack().getType().name().toUpperCase().endsWith("_SHULKER_BOX")) {
                    new ShulkerBoxPreviewMenu(listing, player, category, search, sortingMethod, sortingDirection).open(player);
                    return;
                }

                if (!Fadah.getINSTANCE().getEconomy().has(player, listing.price())
                        || ListingCache.getListing(listing.id()) == null
                        || (Config.STRICT_CHECKS.toBoolean() && Fadah.getINSTANCE().getDatabase().getListing(listing.id()) == null)) {
                    player.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString()));
                    return;
                }
                new ConfirmPurchaseMenu(listing, player, category, search, sortingMethod, sortingDirection).open(player);
            }));
        }
    }

    //<editor-fold desc="Navigation">
    private void addPaginationControls() {
        if (page > 0) {
            setItem(48, GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> previousPage());
        }

        if (listings != null && listings.size() >= index + 1) {
            setItem(50, GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> nextPage());
        }
    }

    private void addNavigationButtons() {
        setItem(0, GuiHelper.constructButton(GuiButtonType.SCROLL_PREVIOUS), e -> {
            scrollUp();
        });
        setItem(45, GuiHelper.constructButton(GuiButtonType.SCROLL_NEXT), e -> {
            scrollDown();
        });

        setItem(53, new ItemBuilder(Material.PLAYER_HEAD).skullOwner(player)
                .name(Menus.MAIN_PROFILE_NAME.toFormattedString(Lang.WORD_YOUR.toString()))
                .addLore(Menus.MAIN_PROFILE_LORE.toLore()).build(), e -> new ProfileMenu(player, player).open(player));

    }

    private void addFilterButtons() {
        // Filter Type Cycle
        SortingMethod prev = sortingMethod.previous();
        SortingMethod next = sortingMethod.next();
        removeItem(47);
        setItem(47, new ItemBuilder(Menus.MAIN_FILTER_TYPE_ICON.toMaterial())
                .name(Menus.MAIN_FILTER_TYPE_NAME.toFormattedString())
                .modelData(Menus.MAIN_FILTER_TYPE_MODEL_DATA.toInteger())
                .addLore(Menus.MAIN_FILTER_TYPE_LORE.toLore((prev == null ? "None" : prev.getFriendlyName()),
                        sortingMethod.getFriendlyName(), (next == null ? "None" : next.getFriendlyName())))
                .build(), e -> {
            if (e.isLeftClick()) {
                if (sortingMethod.previous() == null) return;
                this.sortingMethod = sortingMethod.previous();
                populatePage();
                addFilterButtons();
            }
            if (e.isRightClick()) {
                if (sortingMethod.next() == null) return;
                this.sortingMethod = sortingMethod.next();
                populatePage();
                addFilterButtons();
            }
        });

        // Search
        removeItem(49);
        setItem(49, new ItemBuilder(Menus.MAIN_SEARCH_ICON.toMaterial())
                .name(Menus.MAIN_SEARCH_NAME.toFormattedString())
                .modelData(Menus.MAIN_SEARCH_MODEL_DATA.toInteger())
                .lore(Menus.MAIN_SEARCH_LORE.toLore()).build(), e ->
                new SearchMenu(player, Menus.MAIN_SEARCH_PLACEHOLDER.toString(), search ->
                        new MainMenu(category, player, search, sortingMethod, sortingDirection).open(player)));

        // Filter Direction Toggle
        String asc = StringUtils.formatPlaceholders(sortingDirection == SortingDirection.ASCENDING
                        ? Menus.MAIN_FILTER_DIRECTION_SELECTED.toFormattedString()
                        : Menus.MAIN_FILTER_DIRECTION_NOT_SELECTED.toFormattedString(),
                sortingMethod.getLang(SortingDirection.ASCENDING));
        String desc = StringUtils.formatPlaceholders(sortingDirection == SortingDirection.DESCENDING
                        ? Menus.MAIN_FILTER_DIRECTION_SELECTED.toFormattedString()
                        : Menus.MAIN_FILTER_DIRECTION_NOT_SELECTED.toFormattedString(),
                sortingMethod.getLang(SortingDirection.DESCENDING));

        removeItem(51);
        setItem(51, new ItemBuilder(Menus.MAIN_FILTER_DIRECTION_ICON.toMaterial())
                        .name(Menus.MAIN_FILTER_DIRECTION_NAME.toFormattedString())
                        .modelData(Menus.MAIN_FILTER_DIRECTION_MODEL_DATA.toInteger())
                        .lore(Menus.MAIN_FILTER_DIRECTION_LORE.toLore(asc, desc)).build(), e -> {
                    this.sortingDirection = sortingDirection == SortingDirection.ASCENDING
                            ? SortingDirection.DESCENDING
                            : SortingDirection.ASCENDING;
                    populatePage();
                    addFilterButtons();
                }
        );
    }
    //</editor-fold>


    //<editor-fold desc="Jank">
    @Override
    protected void paginationEmpty() {
        setItems(new int[]{22, 23, 31, 32}, new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial())
                .name(Menus.NO_ITEM_FOUND_NAME.toFormattedString()).modelData(Menus.NO_ITEM_FOUND_MODEL_DATA.toInteger()).lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build());
    }

    @Override
    public List<PaginatedItem> scrollbarItems() {
        return new ArrayList<>();
    }
    //</editor-fold>
}
