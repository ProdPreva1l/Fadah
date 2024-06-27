package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
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
        super(LayoutManager.MenuType.MAIN.getLayout().guiSize(), LayoutManager.MenuType.MAIN.getLayout().guiTitle(), player, LayoutManager.MenuType.MAIN);
        this.category = category;
        this.listings = new ArrayList<>(ListingCache.getListings().values());

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

        List<Integer> fillerSlots = getLayout().fillerSlots();
        if (!fillerSlots.isEmpty()) {
            setItems(fillerSlots.stream().mapToInt(Integer::intValue).toArray(),
                    GuiHelper.constructButton(GuiButtonType.BORDER));
        }

        setScrollbarSlots(getLayout().scrollbarSlots());
        setPaginationMappings(getLayout().paginationSlots());

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
                    .setAttributes(null)
                    .flags(ItemFlag.HIDE_ENCHANTS,
                            ItemFlag.HIDE_ATTRIBUTES,
                            ItemFlag.HIDE_UNBREAKABLE,
                            ItemFlag.HIDE_DESTROYS,
                            ItemFlag.HIDE_PLACED_ON,
                            ItemFlag.HIDE_DYE,
                            ItemFlag.HIDE_POTION_EFFECTS);
            if (category == cat) {
                itemBuilder.name(StringUtils.colorize(cat.name() + "&r " + Lang.CATEGORY_SELECTED.toFormattedString()))
                        .enchant(Enchantment.DURABILITY);
                itemBuilder.flags(ItemFlag.HIDE_ENCHANTS);
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

            String buyMode = listing.isBiddable()
                    ? getLang().getStringFormatted("listing.lore-buy.bidding")
                    : getLang().getStringFormatted("listing.lore-buy.buy-it-now");

            ItemBuilder itemStack = new ItemBuilder(listing.getItemStack().clone())
                    .addLore(getLang().getLore("listing.lore-body",
                            listing.getOwnerName(), listing.getCategoryID(), buyMode, new DecimalFormat(Config.DECIMAL_FORMAT.toString())
                                    .format(listing.getPrice()), TimeUtil.formatTimeUntil(listing.getDeletionDate())));

            if (player.getUniqueId().equals(listing.getOwner())) {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.own-listing"));
            } else if (Fadah.getINSTANCE().getEconomy().has(player, listing.getPrice())) {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.buy"));
            } else {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.too-expensive"));
            }
            if (listing.getItemStack().getType().name().toUpperCase().endsWith("_SHULKER_BOX")) {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.is-shulker"));
            }

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                if (e.isShiftClick()) {
                    if (listing.cancel(((Player) e.getWhoClicked()))) {
                        updatePagination();
                    }
                    return;
                }

                if (e.isRightClick() && listing.getItemStack().getType().name().toUpperCase().endsWith("_SHULKER_BOX")) {
                    new ShulkerBoxPreviewMenu(listing, player, category, search, sortingMethod, sortingDirection).open(player);
                    return;
                }

                if (listing.isOwner(player)) {
                    player.sendMessage(Lang.PREFIX.toFormattedString() + Lang.OWN_LISTING.toFormattedString());
                    return;
                }

                if (!Fadah.getINSTANCE().getEconomy().has(player, listing.getPrice())) {
                    player.sendMessage(Lang.PREFIX.toFormattedString() + Lang.TOO_EXPENSIVE.toFormattedString());
                    return;
                }

                if (ListingCache.getListing(listing.getId()) == null
                        || (Config.STRICT_CHECKS.toBoolean() && Fadah.getINSTANCE().getDatabase().getListing(listing.getId()) == null)) {
                    player.sendMessage(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString());
                    return;
                }

                new ConfirmPurchaseMenu(listing, player, category, search, sortingMethod, sortingDirection).open(player);
            }));
        }
    }

    @Override
    protected void addPaginationControls() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_ONE, 48),
                GuiHelper.constructButton(GuiButtonType.BORDER));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_TWO,50),
                GuiHelper.constructButton(GuiButtonType.BORDER));
        if (page > 0) {
            setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_ONE, 48),
                    GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> previousPage());
        }

        if (listings != null && listings.size() >= index + 1) {
            setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_TWO,50),
                    GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> nextPage());
        }
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.SCROLLBAR_CONTROL_ONE,0),
                GuiHelper.constructButton(GuiButtonType.SCROLL_PREVIOUS), e -> scrollUp());
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.SCROLLBAR_CONTROL_TWO,45),
                GuiHelper.constructButton(GuiButtonType.SCROLL_NEXT), e -> scrollDown());

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PROFILE,53),
                new ItemBuilder(Material.PLAYER_HEAD).skullOwner(player)
                .name(getLang().getStringFormatted("profile-button.name", "&e&l{0} Profile", StringUtils.capitalize(Lang.WORD_YOUR.toString())))
                .addLore(getLang().getLore("profile-button.lore", List.of("&fClick to view your profile!")))
                        .build(), e -> new ProfileMenu(player, player).open(player));
    }

    private void addFilterButtons() {
        // Filter Type Cycle
        SortingMethod prev = sortingMethod.previous();
        SortingMethod next = sortingMethod.next();
        List<String> defFiltLore = List.of(
                "&7Left Click to cycle up",
                "&7Right Click to cycle down",
                "&8-------------------------",
                "&f{0}",
                "&8> &e{1}",
                "&f{2}",
                "&8-------------------------"
        );
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.FILTER,47));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.FILTER,47),
                new ItemBuilder(getLang().getAsMaterial("filter.change-type.icon", Material.PUFFERFISH))
                .name(getLang().getStringFormatted("filter.change-type.name", "&eListing Filter"))
                .modelData(getLang().getInt("filter.change-type.model-data"))
                .addLore(getLang().getLore("filter.change-type.lore", defFiltLore, (prev == null ? Lang.WORD_NONE.toString() : prev.getFriendlyName()),
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
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.SEARCH,49));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.SEARCH,49),
                new ItemBuilder(getLang().getAsMaterial("filter.search.icon", Material.OAK_SIGN))
                .name(getLang().getStringFormatted("filter.search.name", "&3&lSearch"))
                .modelData(getLang().getInt("filter.search.model-data"))
                .lore(getLang().getLore("filter.search.lore")).build(), e ->
                new SearchMenu(player, getLang().getString("filter.search.placeholder", "Search Query..."), search ->
                        new MainMenu(category, player, search, sortingMethod, sortingDirection).open(player)));

        // Filter Direction Toggle
        List<String> defDirLore = List.of(
                "&7Click To Toggle",
                "&8-------------------------",
                "{0}",
                "{1}",
                "&8-------------------------"
        );
        String asc = StringUtils.formatPlaceholders(sortingDirection == SortingDirection.ASCENDING
                        ? getLang().getStringFormatted("filter.change-direction.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("filter.change-direction.options.not-selected", "&f{0}"),
                sortingMethod.getLang(SortingDirection.ASCENDING));
        String desc = StringUtils.formatPlaceholders(sortingDirection == SortingDirection.DESCENDING
                        ? getLang().getStringFormatted("filter.change-direction.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("filter.change-direction.options.not-selected", "&f{0}"),
                sortingMethod.getLang(SortingDirection.DESCENDING));

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.FILTER_DIRECTION,51));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.FILTER_DIRECTION,51),
                new ItemBuilder(getLang().getAsMaterial("filter.change-direction.icon", Material.CLOCK))
                        .name(getLang().getStringFormatted("filter.change-direction.name", "&eFilter Direction"))
                        .modelData(getLang().getInt("filter.change-direction.model-data"))
                        .lore(getLang().getLore("filter.change-direction.lore", defDirLore, asc, desc)).build(), e -> {
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
        this.listings.addAll(ListingCache.getListings().values());

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
}
