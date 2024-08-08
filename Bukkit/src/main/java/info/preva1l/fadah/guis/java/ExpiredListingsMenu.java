package info.preva1l.fadah.guis.java;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.History;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;

public class ExpiredListingsMenu extends PaginatedFastInv {
    private final Player viewer;
    private final OfflinePlayer owner;
    private final List<CollectableItem> expiredItems;

    public ExpiredListingsMenu(Player viewer, OfflinePlayer owner, int page) {
        super(LayoutManager.MenuType.EXPIRED_LISTINGS.getLayout().guiSize(),
                LayoutManager.MenuType.EXPIRED_LISTINGS.getLayout().formattedTitle(viewer.getUniqueId() == owner.getUniqueId()
                        ? Lang.WORD_YOUR.toCapital()
                        : owner.getName()+"'s", owner.getName()+"'s"), viewer, LayoutManager.MenuType.EXPIRED_LISTINGS,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));
        this.viewer = viewer;
        this.owner = owner;
        this.page = page;
        this.expiredItems = ExpiredListingsCache.getExpiredListings(owner.getUniqueId());

        List<Integer> fillerSlots = getLayout().fillerSlots();
        if (!fillerSlots.isEmpty()) {
            setItems(fillerSlots.stream().mapToInt(Integer::intValue).toArray(),
                    GuiHelper.constructButton(GuiButtonType.BORDER));
        }
        setPaginationMappings(getLayout().paginationSlots());

        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    @Override
    protected void fillPaginationItems() {
        for (CollectableItem collectableItem : expiredItems) {
            ItemBuilder itemStack = new ItemBuilder(collectableItem.itemStack().clone())
                    .addLore(getLang().getLore("lore", TimeUtil.formatTimeSince(collectableItem.dateAdded())));

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                MultiLib.getEntityScheduler(viewer).execute(Fadah.getINSTANCE(), () -> {
                    int slot = viewer.getInventory().firstEmpty();
                    if (slot == -1) {
                        viewer.sendMessage(Lang.PREFIX.toFormattedString() + Lang.INVENTORY_FULL.toFormattedString());
                        return;
                    }
                    if (!ExpiredListingsCache.doesItemExist(player.getUniqueId(), collectableItem)) {
                        viewer.sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString()));
                        return;
                    }
                    ExpiredListingsCache.removeItem(owner.getUniqueId(), collectableItem);
                    DatabaseManager.getInstance().deleteSpecific(ExpiredItems.class, ExpiredItems.of(owner.getUniqueId()), collectableItem);
                    viewer.getInventory().setItem(slot, collectableItem.itemStack());

                    updatePagination();

                    // In game logs
                    boolean isAdmin = viewer.getUniqueId() != owner.getUniqueId();
                    HistoricItem historicItem = new HistoricItem(owner.getUniqueId(), Instant.now().toEpochMilli(),
                            isAdmin ? HistoricItem.LoggedAction.EXPIRED_ITEM_ADMIN_CLAIM : HistoricItem.LoggedAction.EXPIRED_ITEM_CLAIM,
                            collectableItem.itemStack(), null, null);
                    HistoricItemsCache.addLog(owner.getUniqueId(), historicItem);
                    DatabaseManager.getInstance().save(History.class, History.of(owner.getUniqueId()));
                },null, 0L);
            }));
        }
    }

    @Override
    protected void addPaginationControls() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_ONE, -1),
                GuiHelper.constructButton(GuiButtonType.BORDER));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_TWO,-1),
                GuiHelper.constructButton(GuiButtonType.BORDER));
        if (page > 0) {
            setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_ONE, -1),
                    GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> previousPage());
        }

        if (expiredItems != null && expiredItems.size() >= index + 1) {
            setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_TWO,-1),
                    GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> nextPage());
        }
    }

    @Override
    protected void updatePagination() {
        this.expiredItems.clear();
        this.expiredItems.addAll(ExpiredListingsCache.getExpiredListings(player.getUniqueId()));
        super.updatePagination();
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.BACK, -1),
                GuiHelper.constructButton(GuiButtonType.BACK), e ->
                new ProfileMenu(viewer, owner).open(viewer));
    }
}
