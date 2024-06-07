package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.cache.CategoryCache;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ScrollBarFastInv extends PaginatedFastInv {
    private final Map<Integer, Integer> scrollbarSlots = new HashMap<>();
    private final List<PaginatedItem> scrollbarItems = new ArrayList<>();

    protected ScrollBarFastInv(int size, @NotNull String title, @NotNull Player player) {
        super(size, title, player);
        scrollbarSlots.put(0, 9);
        scrollbarSlots.put(1, 18);
        scrollbarSlots.put(2, 27);
        scrollbarSlots.put(3, 36);
    }

    protected ScrollBarFastInv(int size, @NotNull String title, @NotNull Player player, List<Integer> scrollbarSlots) {
        super(size, title, player);
        int i = 0;
        for (Integer num : scrollbarSlots) {
            this.scrollbarSlots.put(i, num);
            i++;
        }
    }

    protected void updateScrollbar() {
        scrollbarItems.clear();
        fillScrollbarItems();
        populateScrollbar();
    }

    protected abstract void fillScrollbarItems();

    protected void addScrollbarItem(PaginatedItem item) {
        scrollbarItems.add(item);
    }

    protected void populateScrollbar() {
        int i = 0;
        for (PaginatedItem item : scrollbarItems) {
            if (scrollbarSlots.containsKey(i)) {
                int slot = scrollbarSlots.get(i);
                removeItem(slot);
                setItem(slot, item.itemStack(), item.eventConsumer().andThen((e) -> {
                    updateScrollbar();
                    updatePagination();
                }));
            }
            i++;
        }
    }

    protected void scrollUp() {
        if (scrollbarSlots.containsKey(CategoryCache.getCategories().size() - 1)) return;
        Map<Integer, Integer> newMappings = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : scrollbarSlots.entrySet()) {
            newMappings.put(entry.getKey() + 1, entry.getValue());
        }
        scrollbarSlots.clear();
        scrollbarSlots.putAll(newMappings);
        updateScrollbar();
    }

    protected void scrollDown() {
        if (scrollbarSlots.containsKey(0)) return;
        Map<Integer, Integer> newMappings = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : scrollbarSlots.entrySet()) {
            newMappings.put(entry.getKey() - 1, entry.getValue());
        }
        scrollbarSlots.clear();
        scrollbarSlots.putAll(newMappings);
        updateScrollbar();
    }
}
