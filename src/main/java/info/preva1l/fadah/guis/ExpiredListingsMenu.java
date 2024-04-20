package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpiredListingsMenu extends FastInv {
    private static final int maxItemsPerPage = 21;
    private final Player viewer;
    private final OfflinePlayer owner;
    private final int page;
    private final List<CollectableItem> expiredItems;
    private final Map<Integer, Integer> listingSlot = new HashMap<>();
    private int index = 0;

    public ExpiredListingsMenu(Player viewer, OfflinePlayer owner, int page) {
        super(45, Menus.EXPIRED_LISTINGS_TITLE.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? "Your" : owner.getName()+"'s"));
        this.viewer = viewer;
        this.owner = owner;
        this.page = page;
        this.expiredItems = ExpiredListingsCache.getExpiredListings(owner.getUniqueId());

        fillMappings();

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        addNavigationButtons();
        populateCollectableItems();
        addPaginationControls();

        BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(Fadah.getINSTANCE(), this::refreshMenu, 20L, 20L);
        InventoryEventHandler.tasksToQuit.put(getInventory(), task);
    }

    private void refreshMenu() {
        populateCollectableItems();
        addPaginationControls();
    }

    private void populateCollectableItems() {
        if (expiredItems == null || expiredItems.isEmpty()) {
            setItem(22, new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial()).name(Menus.NO_ITEM_FOUND_NAME.toFormattedString()).lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build());
            return;
        }
        for (int i = 0; i <= maxItemsPerPage; i++) {
            index = maxItemsPerPage * page + i;
            if (index >= expiredItems.size() || i == maxItemsPerPage) break;
            CollectableItem collectableItem = expiredItems.get(index);

            ItemBuilder itemStack = new ItemBuilder(collectableItem.itemStack().clone())
                    .addLore(Menus.EXPIRED_LISTINGS_LORE.toLore(TimeUtil.formatTimeSince(collectableItem.dateAdded())));

            removeItem(listingSlot.get(i));
            setItem(listingSlot.get(i), itemStack.build(), e -> {
                int slot = viewer.getInventory().firstEmpty();
                if (slot >= 36) {
                    viewer.sendMessage(Lang.PREFIX.toFormattedString() + Lang.INVENTORY_FULL.toFormattedString());
                    return;
                }
                ExpiredListingsCache.removeItem(owner.getUniqueId(), collectableItem);
                Fadah.getINSTANCE().getDatabase().removeFromExpiredItems(owner.getUniqueId(), collectableItem);
                viewer.getInventory().setItem(slot, collectableItem.itemStack());
                new ExpiredListingsMenu(viewer, owner, 0).open(viewer);
            });
        }
    }

    private void addPaginationControls() {
        if (page > 0) {
            setItem(39, GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> new ExpiredListingsMenu(viewer, owner, page - 1).open(viewer));
        }
        if (expiredItems != null && expiredItems.size() >= index + 1) {
            setItem(41, GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> new ExpiredListingsMenu(viewer, owner, page + 1).open(viewer));
        }
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
