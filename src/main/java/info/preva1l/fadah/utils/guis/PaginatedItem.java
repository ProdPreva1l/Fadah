package info.preva1l.fadah.utils.guis;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public record PaginatedItem(ItemStack itemStack, Consumer<InventoryClickEvent> eventConsumer) {
}