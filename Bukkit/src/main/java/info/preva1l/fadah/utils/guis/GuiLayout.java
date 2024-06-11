package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.utils.BasicConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public record GuiLayout(
        @NotNull LayoutManager.MenuType menuType,
        @Nullable List<Integer> fillerSlots,
        @Nullable List<Integer> paginationSlots,
        @Nullable List<Integer> scrollbarSlots,
        @NotNull HashMap<Integer, LayoutManager.ButtonType> buttonSlots,
        @NotNull BasicConfig config
) {
}
