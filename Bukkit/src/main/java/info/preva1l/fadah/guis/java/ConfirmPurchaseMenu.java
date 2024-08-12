package info.preva1l.fadah.guis.java;

import info.preva1l.fadah.guis.MenuManager;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfirmPurchaseMenu extends FastInv {
    public ConfirmPurchaseMenu(Player player,
                               Listing listing,
                               LayoutManager.MenuType returnTo,
                               @Nullable String search) {
        super(LayoutManager.MenuType.CONFIRM_PURCHASE.getLayout().guiSize(),
                LayoutManager.MenuType.CONFIRM_PURCHASE.getLayout().guiTitle(),
                LayoutManager.MenuType.CONFIRM_PURCHASE);

        List<Integer> fillerSlots = getLayout().fillerSlots();
        if (!fillerSlots.isEmpty()) {
            setItems(fillerSlots.stream().mapToInt(Integer::intValue).toArray(),
                    GuiHelper.constructButton(GuiButtonType.BORDER));
        }

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.CONFIRM, -1),
                new ItemBuilder(getLang().getAsMaterial("confirm.icon", Material.LIME_CONCRETE))
                        .name(getLang().getStringFormatted("confirm.name", "&a&lCONFIRM"))
                        .modelData(getLang().getInt("confirm.model-data"))
                        .lore(getLang().getLore("confirm.lore")).build(), e -> {
            player.closeInventory();
            listing.purchase(((Player) e.getWhoClicked()));
        });

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.CANCEL, -1),
                new ItemBuilder(getLang().getAsMaterial("cancel.icon", Material.RED_CONCRETE))
                        .name(getLang().getStringFormatted("cancel.name", "&c&lCANCEL"))
                        .modelData(getLang().getInt("cancel.model-data"))
                        .lore(getLang().getLore("cancel.lore")).build(), e -> {
                    if (returnTo == LayoutManager.MenuType.VIEW_LISTINGS) {
                        MenuManager.getInstance().openMenu(player, LayoutManager.MenuType.VIEW_LISTINGS, player);
                        return;
                    }
                    if (returnTo == LayoutManager.MenuType.LISTING_OPTIONS) {
                        MenuManager.getInstance().openMenu(player, LayoutManager.MenuType.LISTING_OPTIONS, listing, search);
                        return;
                    }
                    MenuManager.getInstance().openMenu(player, LayoutManager.MenuType.MAIN, search);
        });

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.ITEM_TO_PURCHASE, -1),
                listing.getItemStack().clone());
    }
}
