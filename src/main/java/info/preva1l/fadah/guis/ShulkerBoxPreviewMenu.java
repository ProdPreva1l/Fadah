package info.preva1l.fadah.guis;

import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.filters.SortingDirection;
import info.preva1l.fadah.utils.filters.SortingMethod;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.GuiButtonType;
import info.preva1l.fadah.utils.guis.GuiHelper;
import info.preva1l.fadah.utils.guis.ItemBuilder;
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
        super(36, listing.itemStack().getItemMeta().getDisplayName().isBlank() ?
                listing.itemStack().getI18NDisplayName() : listing.itemStack().getItemMeta().getDisplayName());
        if (listing.itemStack().getItemMeta() instanceof BlockStateMeta im) {
            if (im.getBlockState() instanceof ShulkerBox shulker) {
                for (int i = 0; i < shulker.getInventory().getSize(); i++) {
                    ItemStack itemStack = shulker.getInventory().getItem(i);
                    if (itemStack == null) {
                        itemStack = new ItemBuilder(Material.AIR).name("&r ").build();
                    }
                    setItem(i, itemStack);
                }
            }
        }

        setItem(31, GuiHelper.constructButton(GuiButtonType.CLOSE), e ->
                new MainMenu(category, player, page, search, sortingMethod, sortingDirection).open(player));
        setItems(new int[]{27, 28, 29, 30, 32, 33, 34, 35}, GuiHelper.constructButton(GuiButtonType.BORDER));
    }
}
