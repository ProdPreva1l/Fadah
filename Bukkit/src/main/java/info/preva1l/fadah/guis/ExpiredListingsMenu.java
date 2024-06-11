package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.HistoricItem;
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
        super(45, Menus.EXPIRED_LISTINGS_TITLE.toFormattedString(viewer.getUniqueId() == owner.getUniqueId()
                        ? Lang.WORD_YOUR.toCapital()
                : owner.getName()+"'s", owner.getName()+"'s"), viewer, LayoutManager.MenuType.EXPIRED_LISTINGS,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));
        this.viewer = viewer;
        this.owner = owner;
        this.page = page;
        this.expiredItems = ExpiredListingsCache.getExpiredListings(owner.getUniqueId());

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }
    
    @Override
    protected void paginationEmpty() {
        setItem(22, new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial())
                .name(Menus.NO_ITEM_FOUND_NAME.toFormattedString()).modelData(
                        Menus.NO_ITEM_FOUND_MODEL_DATA.toInteger()).lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build()
        );
    }

    @Override
    protected void fillPaginationItems() {
        for (CollectableItem collectableItem : expiredItems) {
            ItemBuilder itemStack = new ItemBuilder(collectableItem.itemStack().clone())
                    .addLore(Menus.EXPIRED_LISTINGS_LORE.toLore(TimeUtil.formatTimeSince(collectableItem.dateAdded())));

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                int slot = viewer.getInventory().firstEmpty();
                if (slot >= 36) {
                    viewer.sendMessage(Lang.PREFIX.toFormattedString() + Lang.INVENTORY_FULL.toFormattedString());
                    return;
                }
                ExpiredListingsCache.removeItem(owner.getUniqueId(), collectableItem);
                Fadah.getINSTANCE().getDatabase().removeFromExpiredItems(owner.getUniqueId(), collectableItem);
                viewer.getInventory().setItem(slot, collectableItem.itemStack());
                new ExpiredListingsMenu(viewer, owner, 0).open(viewer);

                // In game logs
                boolean isAdmin = viewer.getUniqueId() != owner.getUniqueId();
                HistoricItem historicItem = new HistoricItem(owner.getUniqueId(), Instant.now().toEpochMilli(),
                        isAdmin ? HistoricItem.LoggedAction.EXPIRED_ITEM_ADMIN_CLAIM : HistoricItem.LoggedAction.EXPIRED_ITEM_CLAIM,
                        collectableItem.itemStack(), null, null);
                HistoricItemsCache.addLog(owner.getUniqueId(), historicItem);
                Fadah.getINSTANCE().getDatabase().addToHistory(owner.getUniqueId(), historicItem);
            }));
        }
    }

    @Override
    protected void addPaginationControls() {
        if (page > 0) {
            setItem(39, GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e ->
                    new ExpiredListingsMenu(viewer, owner, page - 1).open(viewer));
        }
        if (expiredItems != null && expiredItems.size() >= index + 1) {
            setItem(41, GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e ->
                    new ExpiredListingsMenu(viewer, owner, page + 1).open(viewer));
        }
    }

    private void addNavigationButtons() {
        setItem(36, GuiHelper.constructButton(GuiButtonType.BACK), e ->
                new ProfileMenu(viewer, owner).open(viewer));
    }
}
