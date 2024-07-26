package info.preva1l.fadah.guis;

import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.Nullable;

public class ShulkerBoxPreviewMenu extends FastInv {
    public ShulkerBoxPreviewMenu(Listing listing, Player player,
                                 @Nullable Category category,
                                 @Nullable String search,
                                 @Nullable SortingMethod sortingMethod,
                                 @Nullable SortingDirection sortingDirection,
                                 boolean isViewListings,
                                 @Nullable OfflinePlayer listingsPlayer) {
        super(36, listing.getItemStack().getItemMeta().getDisplayName().isBlank()
                ? listing.getItemStack().getItemMeta().getLocalizedName()
                : listing.getItemStack().getItemMeta().getDisplayName(), LayoutManager.MenuType.SHULKER_PREVIEW);
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
            if (isViewListings) {
                assert listingsPlayer != null;
                new ViewListingsMenu(player, listingsPlayer).open(player);
                return;
            }
            new MainMenu(category, player, search, sortingMethod, sortingDirection).open(player);
        });
        setItems(new int[]{27, 28, 29, 30, 32, 33, 34, 35}, GuiHelper.constructButton(GuiButtonType.BORDER));
    }
}
