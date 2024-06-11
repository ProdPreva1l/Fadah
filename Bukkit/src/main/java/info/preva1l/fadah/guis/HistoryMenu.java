package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

public class HistoryMenu extends PaginatedFastInv {
    private final Player viewer;
    private final OfflinePlayer owner;
    private final List<HistoricItem> historicItems;

    public HistoryMenu(Player viewer, OfflinePlayer owner, @Nullable String dateSearch) {
        super(45, Menus.HISTORIC_ITEMS_TITLE.toFormattedString(
                viewer.getUniqueId() == owner.getUniqueId()
                        ? Lang.WORD_YOUR.toCapital()
                        : owner.getName()+"'s", owner.getName()+"'s"), viewer, LayoutManager.MenuType.HISTORY,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));
        this.viewer = viewer;
        this.owner = owner;
        this.historicItems = HistoricItemsCache.getHistory(owner.getUniqueId());

        if (dateSearch != null) {
            this.historicItems.removeIf(historicItem -> !TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()).contains(dateSearch));
        }

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    @Override
    protected void paginationEmpty() {
        setItem(22, new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial())
                .name(Menus.NO_ITEM_FOUND_NAME.toFormattedString())
                .modelData(Menus.NO_ITEM_FOUND_MODEL_DATA.toInteger())
                .lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build());
    }

    @Override
    protected void fillPaginationItems() {
        for (HistoricItem historicItem : historicItems) {
            ItemBuilder itemStack = new ItemBuilder(historicItem.itemStack().clone());
            if (historicItem.purchaserUUID() != null) {
                itemStack.addLore(historicItem.action() == HistoricItem.LoggedAction.LISTING_SOLD
                        ? Menus.HISTORIC_ITEMS_WITH_BUYER_LORE.toLore(
                        historicItem.action().getLocaleActionName(),
                        Bukkit.getOfflinePlayer(historicItem.purchaserUUID()).getName(),
                        new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(historicItem.price()),
                        TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()))

                        : Menus.HISTORIC_ITEMS_WITH_SELLER_LORE.toLore(
                        historicItem.action().getLocaleActionName(),
                        Bukkit.getOfflinePlayer(historicItem.purchaserUUID()).getName(),
                        new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(historicItem.price()),
                        TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()))
                );
            } else if (historicItem.price() != null && historicItem.price() != 0d) {
                itemStack.addLore(Menus.HISTORIC_ITEMS_WITH_PRICE_LORE.toLore(
                        historicItem.action().getLocaleActionName(),
                        new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(historicItem.price()),
                        TimeUtil.formatTimeToVisualDate(historicItem.loggedDate())
                ));
            } else {
                itemStack.addLore(Menus.HISTORIC_ITEMS_LORE.toLore(
                        historicItem.action().getLocaleActionName(),
                        TimeUtil.formatTimeToVisualDate(historicItem.loggedDate())
                ));
            }
            addPaginationItem(new PaginatedItem(itemStack.build(), (e) -> {}));
        }
    }

    @Override
    protected void addPaginationControls() {
        setItem(39, GuiHelper.constructButton(GuiButtonType.BORDER));
        setItem(41, GuiHelper.constructButton(GuiButtonType.BORDER));
        if (page > 0) {
            setItem(39, GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> previousPage());
        }
        if (historicItems != null && historicItems.size() >= index + 1) {
            setItem(41, GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> nextPage());
        }
    }

    private void addNavigationButtons() {
        setItem(36, GuiHelper.constructButton(GuiButtonType.BACK), e -> new ProfileMenu(viewer, owner).open(viewer));

        setItem(40, new ItemBuilder(Menus.HISTORY_SEARCH_ICON.toMaterial()).name(Menus.HISTORY_SEARCH_NAME.toFormattedString())
                .modelData(Menus.HISTORY_SEARCH_MODEL_DATA.toInteger())
                .lore(Menus.HISTORY_SEARCH_LORE.toLore()).build(), e ->
                new SearchMenu(viewer, Menus.HISTORY_SEARCH_PLACEHOLDER.toString(), search -> new HistoryMenu(viewer, owner, search).open(viewer)));
    }
}