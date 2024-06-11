package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.filters.SortingDirection;
import info.preva1l.fadah.utils.filters.SortingMethod;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.GuiButtonType;
import info.preva1l.fadah.utils.guis.GuiHelper;
import info.preva1l.fadah.utils.guis.LayoutManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ConfirmPurchaseMenu extends FastInv {

    public ConfirmPurchaseMenu(Listing listing, Player player, @Nullable Category category, @Nullable String search,
                               @Nullable SortingMethod sortingMethod, @Nullable SortingDirection sortingDirection) {
        super(54, Menus.CONFIRM_TITLE.toFormattedString(), LayoutManager.MenuType.CONFIRM_PURCHASE);

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        setItem(30, GuiHelper.constructButton(GuiButtonType.CONFIRM), e -> {
            player.closeInventory();
            listing.purchase(((Player) e.getWhoClicked()));
        });

        setItem(32, GuiHelper.constructButton(GuiButtonType.CANCEL), e ->
                new MainMenu(category, player, search, sortingMethod, sortingDirection).open(player));

        setItem(22, listing.getItemStack().clone());
    }
}
