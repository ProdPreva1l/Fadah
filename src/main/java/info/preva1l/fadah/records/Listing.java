package info.preva1l.fadah.records;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record Listing(
        @NotNull UUID id,
        @NotNull UUID owner,
        @NotNull String ownerName,
        @NotNull ItemStack itemStack,
        String categoryID,
        double price,
        long creationDate,
        long deletionDate
) {
    public boolean isOwner(@NotNull Player player) {
        return player.getUniqueId().equals(this.owner);
    }
    public boolean isOwner(@NotNull UUID uuid) {
        return this.owner.equals(uuid);
    }
}