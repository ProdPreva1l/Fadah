package info.preva1l.fadah.guis.java;

import info.preva1l.fadah.guis.MenuManager;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.Nullable;

public class ShulkerBoxPreviewMenu extends FastInv {
    public ShulkerBoxPreviewMenu(Player player,
                                 Listing listing,
                                 LayoutManager.MenuType returnTo,
                                 @Nullable OfflinePlayer listingsPlayer) {
        super(36, StringUtils.extractItemName(listing.getItemStack()), LayoutManager.MenuType.SHULKER_PREVIEW);
        if (listing.getItemStack().getItemMeta() instanceof BlockStateMeta im) {
            if (im.getBlockState() instanceof ShulkerBox shulker) {
                for (int i = 0; i < shulker.getInventory().getSize(); i++) {
                    ItemStack itemStack = shulker.getInventory().getItem(i);
                    if (itemStack == null) {
                        itemStack = new ItemBuilder(Material.AIR).build();
                    }
                    setItem(i, itemStack);
                }
            }
        }

        setItem(31, GuiHelper.constructButton(GuiButtonType.CLOSE), e -> {
            if (returnTo == LayoutManager.MenuType.VIEW_LISTINGS) {
                assert listingsPlayer != null;
                MenuManager.getInstance().openMenu(player, LayoutManager.MenuType.VIEW_LISTINGS, listingsPlayer);
                return;
            }
            if (returnTo == LayoutManager.MenuType.LISTING_OPTIONS) {
                MenuManager.getInstance().openMenu(player, LayoutManager.MenuType.LISTING_OPTIONS, listing);
                return;
            }
            MenuManager.getInstance().openMenu(player, LayoutManager.MenuType.MAIN);
        });
        setItems(new int[]{27, 28, 29, 30, 32, 33, 34, 35}, GuiHelper.constructButton(GuiButtonType.BORDER));
    }
}
