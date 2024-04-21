package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
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
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryMenu extends FastInv {
    private static final int maxItemsPerPage = 21;
    private final Player viewer;
    private final OfflinePlayer owner;
    private final int page;
    private final String dateSearch;
    private final List<HistoricItem> historicItems;
    private final Map<Integer, Integer> listingSlot = new HashMap<>();
    private int index = 0;

    public HistoryMenu(Player viewer, OfflinePlayer owner, int page, @Nullable String dateSearch) {
        super(45, Menus.HISTORIC_ITEMS_TITLE.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s"));
        this.viewer = viewer;
        this.owner = owner;
        this.page = page;
        this.dateSearch = dateSearch;
        this.historicItems = HistoricItemsCache.getHistory(owner.getUniqueId());

        if (dateSearch != null) {
            this.historicItems.removeIf(historicItem -> !TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()).contains(dateSearch));
        }

        fillMappings();

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        addNavigationButtons();
        populateHistory();
        addPaginationControls();
    }

    private void populateHistory() {
        if (historicItems == null || historicItems.isEmpty()) {
            setItem(22, new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial()).name(Menus.NO_ITEM_FOUND_NAME.toFormattedString()).lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build());
            return;
        }
        for (int i = 0; i <= maxItemsPerPage; i++) {
            index = maxItemsPerPage * page + i;
            if (index >= historicItems.size() || i == maxItemsPerPage) break;
            HistoricItem historicItem = historicItems.get(index);

            ItemBuilder itemStack = new ItemBuilder(historicItem.itemStack().clone());
            if (historicItem.purchaserUUID() != null) {
                itemStack.addLore(historicItem.action() == HistoricItem.LoggedAction.LISTING_SOLD ? Menus.HISTORIC_ITEMS_WITH_BUYER_LORE.toLore(
                        historicItem.action().getLocaleActionName(),
                        Bukkit.getOfflinePlayer(historicItem.purchaserUUID()).getName(),
                        new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(historicItem.price()),
                        TimeUtil.formatTimeToVisualDate(historicItem.loggedDate())) : Menus.HISTORIC_ITEMS_WITH_SELLER_LORE.toLore(
                        historicItem.action().getLocaleActionName(),
                        Bukkit.getOfflinePlayer(historicItem.purchaserUUID()).getName(),
                        new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(historicItem.price()),
                        TimeUtil.formatTimeToVisualDate(historicItem.loggedDate())));
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

            removeItem(listingSlot.get(i));
            setItem(listingSlot.get(i), itemStack.build());
        }
    }

    private void addPaginationControls() {
        if (page > 0) {
            setItem(39, GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> new HistoryMenu(viewer, owner, page - 1, dateSearch).open(viewer));
        }
        if (historicItems != null && historicItems.size() >= index + 1) {
            setItem(41, GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> new HistoryMenu(viewer, owner, page + 1, dateSearch).open(viewer));
        }
        setItem(40, new ItemBuilder(Menus.HISTORY_SEARCH_ICON.toMaterial()).name(Menus.HISTORY_SEARCH_NAME.toFormattedString())
                .lore(Menus.HISTORY_SEARCH_LORE.toLore()).build(), e ->
                new SearchMenu(viewer, Menus.HISTORY_SEARCH_PLACEHOLDER.toString(), search -> new HistoryMenu(viewer, owner, page, search).open(viewer)));

    }

    private void addNavigationButtons() {
        setItem(36, GuiHelper.constructButton(GuiButtonType.BACK), e -> new ProfileMenu(viewer, owner).open(viewer));
    }

    private void fillMappings() {
        listingSlot.put(0, 10);
        listingSlot.put(1, 11);
        listingSlot.put(2, 12);
        listingSlot.put(3, 13);
        listingSlot.put(4, 14);
        listingSlot.put(5, 15);
        listingSlot.put(6, 16);
        listingSlot.put(7, 19);
        listingSlot.put(8, 20);
        listingSlot.put(9, 21);
        listingSlot.put(10, 22);
        listingSlot.put(11, 23);
        listingSlot.put(12, 24);
        listingSlot.put(13, 25);
        listingSlot.put(14, 28);
        listingSlot.put(15, 29);
        listingSlot.put(16, 30);
        listingSlot.put(17, 31);
        listingSlot.put(18, 32);
        listingSlot.put(19, 33);
        listingSlot.put(20, 34);
    }
}