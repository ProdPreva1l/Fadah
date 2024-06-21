package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.utils.BasicConfig;
import info.preva1l.fadah.utils.LanguageConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public record GuiLayout(
        @NotNull LayoutManager.MenuType menuType,
        @NotNull List<Integer> fillerSlots,
        @NotNull List<Integer> paginationSlots,
        @NotNull List<Integer> scrollbarSlots,
        @NotNull List<Integer> noItems,
        @NotNull HashMap<LayoutManager.ButtonType, Integer> buttonSlots,
        @NotNull String guiTitle,
        int guiSize,
        @NotNull LanguageConfig language,
        @NotNull BasicConfig extraConfig
) {
}
