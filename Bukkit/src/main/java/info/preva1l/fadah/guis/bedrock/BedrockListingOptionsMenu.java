package info.preva1l.fadah.guis.bedrock;

import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.LayoutManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BedrockListingOptionsMenu extends FastInv {
    public BedrockListingOptionsMenu(@NotNull Player player) {
        super(45, LayoutManager.MenuType.LISTING_OPTIONS);
    }
}
