package info.preva1l.fadah.guis;

import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.guis.bedrock.BedrockFiltersMenu;
import info.preva1l.fadah.guis.bedrock.BedrockListingOptionsMenu;
import info.preva1l.fadah.guis.bedrock.BedrockMainMenu;
import info.preva1l.fadah.guis.java.MainMenu;
import info.preva1l.fadah.guis.java.NewListingMenu;
import info.preva1l.fadah.hooks.impl.BedrockHook;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.utils.guis.LayoutManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MenuManager {
    private static MenuManager instance;

    public void openMenu(@NotNull Player player, @NotNull LayoutManager.MenuType menuType, Object... extraArgs) {
        if (BedrockHook.shouldShowBedrockMenu(player)) {
            openBedrockMenu(player, menuType, extraArgs);
        } else {
            openJavaMenu(player, menuType, extraArgs);
        }
    }

    private void openBedrockMenu(@NotNull Player player, @NotNull LayoutManager.MenuType menuType, Object... extraArgs) {
        switch (menuType) {
            case MAIN -> new BedrockMainMenu(player, (Category) extraArgs[0], (String) extraArgs[1], (SortingMethod) extraArgs[2], (SortingDirection) extraArgs[3]).open(player);
            case LISTING_OPTIONS -> new BedrockListingOptionsMenu(player).open(player);
            case FILTERS -> new BedrockFiltersMenu(player).open(player);
            default -> openJavaMenu(player, menuType, extraArgs);
        }
    }

    private void openJavaMenu(@NotNull Player player, @NotNull LayoutManager.MenuType menuType, Object... extraArgs) {
        switch (menuType) {
            case MAIN -> new MainMenu(player, (Category) extraArgs[0], (String) extraArgs[1], (SortingMethod) extraArgs[2], (SortingDirection) extraArgs[3]).open(player);
            case NEW_LISTING -> new NewListingMenu(player, (double) extraArgs[0]).open(player);
        }
    }

    public static MenuManager getInstance() {
        if (instance == null) {
            instance = new MenuManager();
        }
        return instance;
    }
}
