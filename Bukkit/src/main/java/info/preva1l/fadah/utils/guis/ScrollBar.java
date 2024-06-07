package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.cache.CategoryCache;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface ScrollBar {
    Map<Integer, Integer> scrollbarSlots();
    List<PaginatedItem> scrollbarItems();

    // Jankery start
    void removeItem(int slot);
    void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> eventConsumer);
    // Jankery end

    void fillScrollbarItems();

    default void addScrollbarItem(PaginatedItem item) {
        scrollbarItems().add(item);
    }

    default void populateScrollbar() {
        int i = 0;
        for (PaginatedItem item : scrollbarItems()) {
            if (scrollbarSlots().containsKey(i)) {
                int slot = scrollbarSlots().get(i);
                removeItem(slot);
                setItem(slot, item.itemStack(), item.eventConsumer());
            }
            i++;
        }
    }

    default void scrollUp() {
        if (scrollbarSlots().containsKey(CategoryCache.getCategories().size() - 1)) return;
        Map<Integer, Integer> newMappings = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : scrollbarSlots().entrySet()) {
            newMappings.put(entry.getKey() + 1, entry.getValue());
        }
        scrollbarSlots().clear();
        scrollbarSlots().putAll(newMappings);
        populateScrollbar();
    }

    default void scrollDown() {
        if (scrollbarSlots().containsKey(0)) return;
        Map<Integer, Integer> newMappings = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : scrollbarSlots().entrySet()) {
            newMappings.put(entry.getKey() - 1, entry.getValue());
        }
        scrollbarSlots().clear();
        scrollbarSlots().putAll(newMappings);
        populateScrollbar();
    }
}
