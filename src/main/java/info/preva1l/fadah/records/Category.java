package info.preva1l.fadah.records;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public record Category(
        @NotNull String id,
        @NotNull String name,
        int priority,
        int modelData,
        @NotNull Material icon,
        @NotNull List<String> description,
        @Nullable Set<Material> materials,
        boolean isCustomItems,
        @Nullable CustomItemMode customItemMode,
        @Nullable Set<String> customItemIds
) {
    public enum CustomItemMode {
        API, ECO_ITEMS
    }
}