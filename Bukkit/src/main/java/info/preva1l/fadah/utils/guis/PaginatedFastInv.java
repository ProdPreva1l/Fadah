package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Menus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class PaginatedFastInv extends FastInv {
    protected final Player player;

    protected int page = 0;
    protected int index = 0;
    private List<Integer> paginationMappings;
    private final List<PaginatedItem> paginatedItems = new ArrayList<>();
    protected boolean needsClearing = false;

    protected PaginatedFastInv(int size, @NotNull String title, @NotNull Player player, LayoutManager.MenuType menuType) {
        super(size, title, menuType);
        this.player = player;
        this.paginationMappings = List.of(
                11, 12, 13, 14, 15, 16, 20,
                21, 22, 23, 24, 25, 29, 30,
                31, 32, 33, 34, 38, 39, 40,
                41, 42, 43);

        BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(Fadah.getINSTANCE(), this::updatePagination, 20L, 20L);
        InventoryEventHandler.tasksToQuit.put(getInventory(), task);
    }

    protected PaginatedFastInv(int size, @NotNull String title, @NotNull Player player, LayoutManager.MenuType menuType, @NotNull List<Integer> paginationMappings) {
        super(size, title, menuType);
        this.player = player;
        this.paginationMappings = paginationMappings;

        BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(Fadah.getINSTANCE(), this::updatePagination, 20L, 20L);
        InventoryEventHandler.tasksToQuit.put(getInventory(), task);
    }

    protected void setPaginationMappings(List<Integer> list) {
        this.paginationMappings = list;
    }

    protected void nextPage() {
        if (paginatedItems == null || paginatedItems.size() < index + 1) {
            return;
        }
        page++;
        populatePage();
        addPaginationControls();
    }

    protected void previousPage() {
        if (page == 0) {
            return;
        }
        page--;
        populatePage();
        addPaginationControls();
    }

    protected void populatePage() {
        int maxItemsPerPage = paginationMappings.size();
        boolean empty = paginatedItems == null || paginatedItems.isEmpty();
        if (empty) {
            if (needsClearing) {
                for (Integer paginationMapping : paginationMappings) removeItem(paginationMapping);
                needsClearing = false;
            }
            paginationEmpty();
            return;
        }

        needsClearing = true;
        
        for (int i = 0; i < maxItemsPerPage; i++) {
            removeItem(paginationMappings.get(i));
            index = maxItemsPerPage * page + i;
            if (index >= paginatedItems.size()) continue;
            PaginatedItem item = paginatedItems.get(index);
            setItem(paginationMappings.get(i), item.itemStack(), item.eventConsumer());
        }
    }

    protected void updatePagination() {
        paginatedItems.clear();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    protected void paginationEmpty() {
        List<Integer> noItems = getLayout().noItems();
        if (!noItems.isEmpty()) {
            setItems(noItems.stream().mapToInt(Integer::intValue).toArray(),
                    new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial())
                            .name(Menus.NO_ITEM_FOUND_NAME.toFormattedString())
                            .modelData(Menus.NO_ITEM_FOUND_MODEL_DATA.toInteger())
                            .lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build());
        }
    }

    protected abstract void fillPaginationItems();

    protected abstract void addPaginationControls();

    protected void addPaginationItem(PaginatedItem item) {
        paginatedItems.add(item);
    }
}
