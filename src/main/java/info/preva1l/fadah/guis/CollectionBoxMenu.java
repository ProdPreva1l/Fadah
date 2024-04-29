package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionBoxMenu extends FastInv {
    private static final int maxItemsPerPage = 21;
    private final Player viewer;
    private final OfflinePlayer owner;
    private final int page;
    private final List<CollectableItem> collectionBox;
    private final Map<Integer, Integer> listingSlot = new HashMap<>();
    private int index = 0;

    public CollectionBoxMenu(Player viewer, OfflinePlayer owner, int page) {
        super(45, Menus.COLLECTION_BOX_TITLE.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s"));
        this.viewer = viewer;
        this.owner = owner;
        this.page = page;
        this.collectionBox = CollectionBoxCache.getCollectionBox(owner.getUniqueId());

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
        if (collectionBox == null || collectionBox.isEmpty()) {
            setItem(22, new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial())
                    .name(Menus.NO_ITEM_FOUND_NAME.toFormattedString()).modelData(Menus.NO_ITEM_FOUND_MODEL_DATA.toInteger()).lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build());
            return;
        }
        for (int i = 0; i <= maxItemsPerPage; i++) {
            index = maxItemsPerPage * page + i;
            if (index >= collectionBox.size() || i == maxItemsPerPage) break;
            CollectableItem collectableItem = collectionBox.get(index);

            removeItem(listingSlot.get(i));
            setItem(listingSlot.get(i), new ItemBuilder(collectableItem.itemStack().clone())
                    .lore(Menus.COLLECTION_BOX_LORE.toLore(TimeUtil.formatTimeSince(collectableItem.dateAdded()))).build(), e -> {
                int slot = viewer.getInventory().firstEmpty();
                if (slot >= 36) {
                    viewer.sendMessage(Lang.PREFIX.toFormattedString() + Lang.INVENTORY_FULL.toFormattedString());
                    return;
                }
                CollectionBoxCache.removeItem(owner.getUniqueId(), collectableItem);
                Fadah.getINSTANCE().getDatabase().removeFromCollectionBox(owner.getUniqueId(), collectableItem);
                viewer.getInventory().setItem(slot, collectableItem.itemStack());

                new CollectionBoxMenu(viewer, owner, 0).open(viewer);


                // In game logs
                boolean isAdmin = viewer.getUniqueId() != owner.getUniqueId();
                HistoricItem historicItem = new HistoricItem(owner.getUniqueId(), Instant.now().toEpochMilli(),
                        isAdmin ? HistoricItem.LoggedAction.COLLECTION_BOX_ADMIN_CLAIM : HistoricItem.LoggedAction.COLLECTION_BOX_CLAIM,
                        collectableItem.itemStack(), null, null);
                HistoricItemsCache.addLog(owner.getUniqueId(), historicItem);
                Fadah.getINSTANCE().getDatabase().addToHistory(owner.getUniqueId(), historicItem);
            });
        }
    }

    private void addPaginationControls() {
        if (page > 0) {
            setItem(39, GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> new CollectionBoxMenu(viewer, owner, page - 1).open(viewer));
        }
        if (collectionBox != null && collectionBox.size() >= index + 1) {
            setItem(41, GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> new CollectionBoxMenu(viewer, owner, page + 1).open(viewer));
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
