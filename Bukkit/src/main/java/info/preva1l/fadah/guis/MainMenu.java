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
import java.util.List;

public class MainMenu extends ScrollBarFastInv {
    private Category category;
    private final List<Listing> listings;

    // Filters
    private final String search;
    private SortingMethod sortingMethod;
    private SortingDirection sortingDirection;

    public MainMenu(@Nullable Category category, @NotNull Player player, @Nullable String search,
                    @Nullable SortingMethod sortingMethod, @Nullable SortingDirection sortingDirection) {
        super(54, Menus.MAIN_TITLE.toFormattedString(), player, LayoutManager.MenuType.MAIN);
        this.category = category;
        this.listings = ListingCache.getListings();

        this.search = search;
        this.sortingMethod = (sortingMethod == null ? SortingMethod.AGE : sortingMethod);
        this.sortingDirection = (sortingDirection == null ? SortingDirection.ASCENDING : sortingDirection);

        listings.sort(this.sortingMethod.getSorter(this.sortingDirection));

        if (category != null) {
            listings.removeIf(listing -> !listing.getCategoryID().equals(category.id()));
        }
        if (search != null) {
            listings.removeIf(listing -> !(listing.getItemStack().getType().name().toUpperCase().contains(search.toUpperCase())
                    || listing.getItemStack().getType().name().toUpperCase().contains(search.replace(" ", "_").toUpperCase()))
                    && !checkForStringInItem(search.toUpperCase(), listing.getItemStack())
                    && !checkForEnchantmentOnBook(search.toUpperCase(), listing.getItemStack()));
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
                } else {
                    this.category = null;
                }

                updatePagination();
                updateScrollbar();
            }));
        }
    }

    @Override
    protected void fillPaginationItems() {
        for (Listing listing : listings) {
            ItemBuilder itemStack = new ItemBuilder(listing.getItemStack().clone())
                    .addLore(Menus.MAIN_LISTING_LORE.toLore(listing.getOwnerName(), listing.getCategoryID(),
                            new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(listing.getPrice()), TimeUtil.formatTimeUntil(listing.getDeletionDate())));

            if (player.getUniqueId().equals(listing.getOwner())) {
                itemStack.addLore(Menus.MAIN_LISTING_FOOTER_OWN_LISTING.toFormattedString());
            } else if (Fadah.getINSTANCE().getEconomy().has(player, listing.getPrice())) {
                itemStack.addLore(Menus.MAIN_LISTING_FOOTER_BUY.toFormattedString());
            } else {
                itemStack.addLore(Menus.MAIN_LISTING_FOOTER_EXPENSIVE.toFormattedString());
            }
            if (listing.getItemStack().getType().name().toUpperCase().endsWith("_SHULKER_BOX")) {
                itemStack.addLore(Menus.MAIN_LISTING_FOOTER_SHULKER.toFormattedString());
            }

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                if (e.isShiftClick()) {
                    listing.cancel(((Player) e.getWhoClicked()));
                    return;
                }

                if (e.isRightClick() && listing.getItemStack().getType().name().toUpperCase().endsWith("_SHULKER_BOX")) {
                    new ShulkerBoxPreviewMenu(listing, player, category, search, sortingMethod, sortingDirection).open(player);
                    return;
                }

                if (!Fadah.getINSTANCE().getEconomy().has(player, listing.getPrice())
                        || ListingCache.getListing(listing.getId()) == null
                        || (Config.STRICT_CHECKS.toBoolean() && Fadah.getINSTANCE().getDatabase().getListing(listing.getId()) == null)) {
                    player.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString()));
                    return;
                }
                new ConfirmPurchaseMenu(listing, player, category, search, sortingMethod, sortingDirection).open(player);
            }));
        }
    }

    @Override
    protected void addPaginationControls() {
        setItem(48, GuiHelper.constructButton(GuiButtonType.BORDER));
        setItem(50, GuiHelper.constructButton(GuiButtonType.BORDER));
        if (page > 0) {
            setItem(48, GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> previousPage());
        }

        if (listings != null && listings.size() >= index + 1) {
            setItem(50, GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> nextPage());
        }
    }

    private void addNavigationButtons() {
        setItem(0, GuiHelper.constructButton(GuiButtonType.SCROLL_PREVIOUS), e -> scrollUp());
        setItem(45, GuiHelper.constructButton(GuiButtonType.SCROLL_NEXT), e -> scrollDown());

        setItem(53, new ItemBuilder(Material.PLAYER_HEAD).skullOwner(player)
                .name(Menus.MAIN_PROFILE_NAME.toFormattedString(StringUtils.capitalize(Lang.WORD_YOUR.toString())))
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
                .addLore(Menus.MAIN_FILTER_TYPE_LORE.toLore((prev == null ? Lang.WORD_NONE.toString() : prev.getFriendlyName()),
                        sortingMethod.getFriendlyName(), (next == null ? Lang.WORD_NONE.toString() : next.getFriendlyName())))
                .build(), e -> {
            if (e.isLeftClick()) {
                if (sortingMethod.previous() == null) return;
                this.sortingMethod = sortingMethod.previous();
                updatePagination();
                addFilterButtons();
            }
            if (e.isRightClick()) {
                if (sortingMethod.next() == null) return;
                this.sortingMethod = sortingMethod.next();
                updatePagination();
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
                    updatePagination();
                    addFilterButtons();
                }
        );
    }

    @Override
    protected void updatePagination() {
        this.listings.clear();
        this.listings.addAll(ListingCache.getListings());

        listings.sort(this.sortingMethod.getSorter(this.sortingDirection));

        if (category != null) {
            listings.removeIf(listing -> !listing.getCategoryID().equals(category.id()));
        }

        if (search != null) {
            listings.removeIf(listing -> !(listing.getItemStack().getType().name().toUpperCase().contains(search.toUpperCase())
                    || listing.getItemStack().getType().name().toUpperCase().contains(search.replace(" ", "_").toUpperCase()))
                    && !checkForStringInItem(search.toUpperCase(), listing.getItemStack())
                    && !checkForEnchantmentOnBook(search.toUpperCase(), listing.getItemStack()));
        }

        super.updatePagination();
    }

    @Override
    protected void paginationEmpty() {
        setItems(new int[]{22, 23, 31, 32}, new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial())
                .name(Menus.NO_ITEM_FOUND_NAME.toFormattedString()).modelData(Menus.NO_ITEM_FOUND_MODEL_DATA.toInteger()).lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build());
    }
}
