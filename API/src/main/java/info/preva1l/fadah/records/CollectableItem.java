package info.preva1l.fadah.records;

import org.bukkit.inventory.ItemStack;

public record CollectableItem(
        ItemStack itemStack,
        long dateAdded
) {
    @Override
    public boolean equals(Object o) {
        if (o instanceof  CollectableItem collectableItem) {
            return collectableItem.dateAdded == this.dateAdded() && collectableItem.itemStack.equals(this.itemStack);
        }
        return false;
    }
}
