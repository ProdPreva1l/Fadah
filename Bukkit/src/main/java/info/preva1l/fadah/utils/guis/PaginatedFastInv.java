package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.Fadah;
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
    private final List<Integer> paginationMappings;
    private final List<PaginatedItem> paginatedItems = new ArrayList<>();

    protected PaginatedFastInv(int size, @NotNull String title, @NotNull Player player) {
        super(size, title);
        this.player = player;
        this.paginationMappings = List.of(
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
                31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                41, 42, 43);

        BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(Fadah.getINSTANCE(), this::populatePage, 20L, 20L);
        InventoryEventHandler.tasksToQuit.put(getInventory(), task);
    }

    protected PaginatedFastInv(int size, @NotNull String title, @NotNull Player player, @NotNull List<Integer> paginationMappings) {
        super(size, title);
        this.player = player;
        this.paginationMappings = paginationMappings;

        BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(Fadah.getINSTANCE(), this::populatePage, 20L, 20L);
        InventoryEventHandler.tasksToQuit.put(getInventory(), task);
    }

    protected void nextPage() {
        if (paginatedItems == null || paginatedItems.size() < index + 1) {
            return;
        }
        page++;
        populatePage();
    }

    protected void previousPage() {
        if (page == 0) {
            return;
        }
        page--;
        populatePage();
    }

    protected void populatePage() {
        int maxItemsPerPage = paginationMappings.size();
        if (paginatedItems == null || paginatedItems.isEmpty()) {
            paginationEmpty();
            return;
        }
        for (int i = 0; i <= maxItemsPerPage; i++) {
            index = maxItemsPerPage * page + i;
            if (index >= paginatedItems.size() || i == maxItemsPerPage) break;
            PaginatedItem item = paginatedItems.get(index);

            removeItem(paginationMappings.get(i));
            setItem(paginationMappings.get(i), item.itemStack(), item.eventConsumer());
        }
    }

    protected abstract void paginationEmpty();

    protected abstract void fillPaginationItems();

    protected void addPaginationItem(PaginatedItem item) {
        paginatedItems.add(item);
    }
}
