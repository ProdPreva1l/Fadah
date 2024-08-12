package info.preva1l.fadah.guis.bedrock;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.MenuManager;
import info.preva1l.fadah.guis.java.ConfirmPurchaseMenu;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.LayoutManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BedrockListingOptionsMenu extends FastInv {
    public BedrockListingOptionsMenu(@NotNull Player player, @NotNull Listing listing) {
        super(LayoutManager.MenuType.LISTING_OPTIONS.getLayout().guiSize(),
                LayoutManager.MenuType.LISTING_OPTIONS.getLayout().guiTitle(),
                LayoutManager.MenuType.LISTING_OPTIONS);


        // cancel button
        if (listing.cancel(player)) {
            MenuManager.getInstance().openMenu(player, LayoutManager.MenuType.MAIN);
        }


        if (listing.getItemStack().getType().name().toUpperCase().endsWith("SHULKER_BOX")) {
            MenuManager.getInstance().openMenu(player, LayoutManager.MenuType.SHULKER_PREVIEW,
                    listing, LayoutManager.MenuType.LISTING_OPTIONS, null);
            return;
        }

        if (listing.isOwner(player)) {
            player.sendMessage(Lang.PREFIX.toFormattedString() + Lang.OWN_LISTING.toFormattedString());
            return;
        }

        if (!Fadah.getINSTANCE().getEconomy().has(player, listing.getPrice())) {
            player.sendMessage(Lang.PREFIX.toFormattedString() + Lang.TOO_EXPENSIVE.toFormattedString());
            return;
        }

        if (ListingCache.getListing(listing.getId()) == null) { // todo: re-add strict checks
            player.sendMessage(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString());
            return;
        }


        new ConfirmPurchaseMenu(listing, player, null, LayoutManager.MenuType.LISTING_OPTIONS).open(player);
    }
}
