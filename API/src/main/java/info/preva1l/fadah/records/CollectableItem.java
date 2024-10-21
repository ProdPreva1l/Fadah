package info.preva1l.fadah.records;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CollectableItem {

    private final UUID id;
    private final UUID owner;
    private final ItemStack itemStack;
    private final long dateAdded;

    public CollectableItem(ItemStack itemStack, long dateAdded) {
        this(UUID.randomUUID(), UUID.randomUUID(), itemStack, dateAdded);
    }

    public CollectableItem(UUID id, UUID owner, ItemStack itemStack, long dateAdded) {
        this.id = id;
        this.owner = owner;
        this.itemStack = itemStack;
        this.dateAdded = dateAdded;
    }

    public UUID id() {
        return id;
    }

    public UUID owner() {
        return owner;
    }

    public ItemStack itemStack() {
        return itemStack;
    }

    public long dateAdded() {
        return dateAdded;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof  CollectableItem collectableItem) {
            return collectableItem.dateAdded == this.dateAdded() && collectableItem.itemStack.equals(this.itemStack);
        }
        return false;
    }
}
