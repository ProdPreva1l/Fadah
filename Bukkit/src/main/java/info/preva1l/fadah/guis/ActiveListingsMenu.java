package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ActiveListingsMenu extends PaginatedFastInv {
    private final Player viewer;
    private final OfflinePlayer owner;
    private final List<Listing> listings;

    public ActiveListingsMenu(Player viewer, OfflinePlayer owner) {
        super(45, Menus.ACTIVE_LISTINGS_TITLE.toFormattedString(viewer.getUniqueId() == owner.getUniqueId()
                ? Lang.WORD_YOUR.toCapital()
                : owner.getName()+"'s", owner.getName()+"'s"), viewer, LayoutManager.MenuType.ACTIVE_LISTINGS,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));
        this.viewer = viewer;
        this.owner = owner;
        this.listings = new ArrayList<>(ListingCache.getListings().values());
        listings.removeIf(listing -> !listing.isOwner(owner.getUniqueId()));

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    @Override
    protected void paginationEmpty() {
        setItem(22, new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial())
                .name(Menus.NO_ITEM_FOUND_NAME.toFormattedString()).modelData(
                        Menus.NO_ITEM_FOUND_MODEL_DATA.toInteger()).lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build()
        );
    }

    @Override
    protected void fillPaginationItems() {
        for (Listing listing : listings) {
            ItemBuilder itemStack = new ItemBuilder(listing.getItemStack().clone())
                    .addLore(Menus.ACTIVE_LISTINGS_LORE.toLore(listing.getCategoryID(), listing.getPrice(),
                            TimeUtil.formatTimeUntil(listing.getDeletionDate())));

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                listing.cancel(viewer);
                updatePagination();
            }));
        }
    }

    @Override
    protected void addPaginationControls() {
        setItem(39, GuiHelper.constructButton(GuiButtonType.BORDER));
        setItem(41, GuiHelper.constructButton(GuiButtonType.BORDER));
        if (page > 0) {
            setItem(39, GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> previousPage());
        }
        if (listings != null && listings.size() >= index + 1) {
            setItem(41, GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> nextPage());
        }
    }

    private void addNavigationButtons() {
        setItem(36, GuiHelper.constructButton(GuiButtonType.BACK), e ->
                new ProfileMenu(viewer, owner).open(viewer));
    }
}