package info.preva1l.fadah.guis;

import com.google.common.collect.Maps;
import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.guis.bedrock.BedrockFiltersMenu;
import info.preva1l.fadah.guis.bedrock.BedrockListingOptionsMenu;
import info.preva1l.fadah.guis.bedrock.BedrockMainMenu;
import info.preva1l.fadah.guis.java.MainMenu;
import info.preva1l.fadah.guis.java.NewListingMenu;
import info.preva1l.fadah.guis.java.ProfileMenu;
import info.preva1l.fadah.guis.java.ShulkerBoxPreviewMenu;
import info.preva1l.fadah.hooks.impl.BedrockHook;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.guis.LayoutManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MenuManager {
    private static MenuManager instance;
    private final Map<UUID, SortingMethod> sortingMethods = Maps.newConcurrentMap();
    private final Map<UUID, SortingDirection> sortingDirections = Maps.newConcurrentMap();
    private final Map<UUID, Category> categories = Maps.newConcurrentMap();

    public void openMenu(@NotNull Player player, @NotNull LayoutManager.MenuType menuType, Object... extraArgs) {
        if (BedrockHook.shouldShowBedrockMenu(player)) {
            openBedrockMenu(player, menuType, extraArgs);
        } else {
            openJavaMenu(player, menuType, extraArgs);
        }
    }

    private void openBedrockMenu(@NotNull Player player, @NotNull LayoutManager.MenuType menuType, Object... extraArgs) {
        switch (menuType) {
            case MAIN -> new BedrockMainMenu(player, (String) extraArgs[0]).open(player);
            case LISTING_OPTIONS -> new BedrockListingOptionsMenu(player, (Listing) extraArgs[0]).open(player);
            case FILTERS -> new BedrockFiltersMenu(player).open(player);
            default -> openJavaMenu(player, menuType, extraArgs);
        }
    }

    private void openJavaMenu(@NotNull Player player, @NotNull LayoutManager.MenuType menuType, Object... extraArgs) {
        switch (menuType) {
            case MAIN -> new MainMenu(player, (String) extraArgs[0]).open(player);
            case NEW_LISTING -> new NewListingMenu(player, (double) extraArgs[0]).open(player);
            case PROFILE -> new ProfileMenu(player, (OfflinePlayer) extraArgs[0]).open(player);
            case SHULKER_PREVIEW -> new ShulkerBoxPreviewMenu(player, (Listing) extraArgs[0], (LayoutManager.MenuType) extraArgs[1], (OfflinePlayer) extraArgs[2]).open(player);
        }
    }

    public SortingMethod getSortMethod(Player player) {
        return sortingMethods.get(player.getUniqueId());
    }

    public void setSortMethod(Player player, SortingMethod sortingMethod) {
        sortingMethods.put(player.getUniqueId(), sortingMethod);
    }

    public SortingDirection getSortDirection(Player player) {
        return sortingDirections.get(player.getUniqueId());
    }

    public void setSortDirection(Player player, SortingDirection sortingMethod) {
        sortingDirections.put(player.getUniqueId(), sortingMethod);
    }

    public Category getCategory(Player player) {
        return categories.get(player.getUniqueId());
    }

    public void setCategory(Player player, @Nullable Category category) {
        categories.put(player.getUniqueId(), category);
    }

    public static MenuManager getInstance() {
        if (instance == null) {
            instance = new MenuManager();
        }
        return instance;
    }
}
