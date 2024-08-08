package info.preva1l.fadah.guis.bedrock;

import info.preva1l.fadah.guis.MenuManager;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BedrockFiltersMenu extends FastInv {
    public BedrockFiltersMenu(@NotNull Player player) {
        super(LayoutManager.MenuType.FILTERS.getLayout().guiSize(), LayoutManager.MenuType.MAIN.getLayout().guiTitle(), LayoutManager.MenuType.FILTERS);

        List<Integer> fillerSlots = getLayout().fillerSlots();
        if (!fillerSlots.isEmpty()) {
            setItems(fillerSlots.stream().mapToInt(Integer::intValue).toArray(),
                    GuiHelper.constructButton(GuiButtonType.BORDER));
        }

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.CONFIRM, -1),
                new ItemBuilder(getLang().getAsMaterial("confirm.icon", Material.LIME_CONCRETE))
                        .name(getLang().getStringFormatted("confirm.name", "&a&lCONFIRM"))
                        .modelData(getLang().getInt("confirm.model-data"))
                        .lore(getLang().getLore("confirm.lore")).build(),
                e -> MenuManager.getInstance().openMenu(player, LayoutManager.MenuType.MAIN));
    }
}
