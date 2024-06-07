package info.preva1l.fadah.records;

import org.bukkit.inventory.ItemStack;

public record CollectableItem(
        ItemStack itemStack,
        long dateAdded
) {
}
