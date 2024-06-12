package info.preva1l.fadah.records;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public abstract class Listing {
    final @NotNull UUID id;
    final @NotNull UUID owner;
    final @NotNull String ownerName;
    final @NotNull ItemStack itemStack;
    final @NotNull String categoryID;
    final double price;
    final double tax;
    final long creationDate;
    final long deletionDate;

    protected Listing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName, @NotNull ItemStack itemStack,
                      @NotNull String categoryID, double price, double tax, long creationDate, long deletionDate) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.itemStack = itemStack;
        this.categoryID = categoryID;
        this.price = price;
        this.tax = tax;
        this.creationDate = creationDate;
        this.deletionDate = deletionDate;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isOwner(@NotNull Player player) {
        return player.getUniqueId().equals(this.owner);
    }

    public boolean isOwner(@NotNull UUID uuid) {
        return this.owner.equals(uuid);
    }

    public abstract void purchase(@NotNull Player buyer);

    public abstract void cancel(@NotNull Player canceller);
}