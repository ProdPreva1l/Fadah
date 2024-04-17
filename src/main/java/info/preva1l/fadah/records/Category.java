package info.preva1l.fadah.records;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public record Category(
        @NotNull String id,
        @NotNull String name,
        int priority,
        @NotNull Material icon,
        @NotNull List<String> description,
        Set<Material> materials,
        boolean isCustomItems,
        Set<String> customItemIds
) {
}