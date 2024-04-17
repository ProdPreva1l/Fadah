package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.filters.SortingDirection;
import info.preva1l.fadah.utils.filters.SortingMethod;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.GuiButtonType;
import info.preva1l.fadah.utils.guis.GuiHelper;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.Nullable;

public class ShulkerBoxPreviewMenu extends FastInv {
    public ShulkerBoxPreviewMenu(Listing listing, Player player,
                                 @Nullable Category category, int page,
                                 @Nullable String search,
                                 @Nullable SortingMethod sortingMethod,
                                 @Nullable SortingDirection sortingDirection) {
        super(45);
        if (listing.itemStack().getItemMeta() instanceof BlockStateMeta im) {
            if (im.getBlockState() instanceof ShulkerBox shulker) {
                for (ItemStack itemStack : shulker.getInventory().getContents()) {
                    if (itemStack == null) {
                        itemStack = new ItemStack(Material.AIR);
                    }
                    addItem(itemStack);
                }
            }
        }

        setItem(40, GuiHelper.constructButton(GuiButtonType.CLOSE), e->new MainMenu(category, player, page, search, sortingMethod, sortingDirection).open(player));
        setItems(new int[]{36,37,38,39,41,42,43,44}, GuiHelper.constructButton(GuiButtonType.GENERIC, Material.BLACK_STAINED_GLASS_PANE,
                StringUtils.colorize("&r "), Menus.BORDER_LORE.toLore()));
    }
}
