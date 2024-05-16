package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.GuiButtonType;
import info.preva1l.fadah.utils.guis.GuiHelper;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProfileMenu extends FastInv {
    public ProfileMenu(@NotNull Player viewer, @NotNull OfflinePlayer owner) {
        super(54, Menus.PROFILE_TITLE.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"));
        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        setItem(20, new ItemBuilder(Material.PLAYER_HEAD).skullOwner(owner)
                .name(Menus.MAIN_PROFILE_NAME.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .addLore(Menus.MAIN_PROFILE_DESCRIPTION.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName(),
                        viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOU.toString() : owner.getName())).build());

        setItem(22, new ItemBuilder(Menus.PROFILE_YOUR_LISTINGS_ICON.toMaterial())
                .name(Menus.PROFILE_YOUR_LISTINGS_NAME.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .modelData(Menus.PROFILE_YOUR_LISTINGS_MODEL_DATA.toInteger())
                .addLore(Menus.PROFILE_YOUR_LISTINGS_LORE.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName()+"'s")).build(), e -> {
            if (viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.active-listings")) {
                new ActiveListingsMenu(viewer, owner, 0).open(viewer);
            } else if (viewer.getUniqueId() == owner.getUniqueId()) {
                new ActiveListingsMenu(viewer, owner, 0).open(viewer);
            }
        });
        setItem(23, new ItemBuilder(Menus.PROFILE_COLLECTION_BOX_ICON.toMaterial())
                .name(Menus.PROFILE_COLLECTION_BOX_NAME.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .modelData(Menus.PROFILE_COLLECTION_BOX_MODEL_DATA.toInteger())
                .addLore(Menus.PROFILE_COLLECTION_BOX_LORE.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName()+"'s")).build(), e -> {
            if (viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.collection-box")) {
                new CollectionBoxMenu(viewer, owner, 0).open(viewer);
            } else if (viewer.getUniqueId() == owner.getUniqueId()) {
                new CollectionBoxMenu(viewer, owner, 0).open(viewer);
            }
        });
        setItem(24, new ItemBuilder(Menus.PROFILE_EXPIRED_LISTINGS_ICON.toMaterial())
                .name(Menus.PROFILE_EXPIRED_LISTINGS_NAME.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .modelData(Menus.PROFILE_EXPIRED_LISTINGS_MODEL_DATA.toInteger())
                .addLore(Menus.PROFILE_EXPIRED_LISTINGS_LORE.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName()+"'s")).build(), e -> {
            if (viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.expired-listings")) {
                new ExpiredListingsMenu(viewer, owner, 0).open(viewer);
            } else if (viewer.getUniqueId() == owner.getUniqueId()) {
                new ExpiredListingsMenu(viewer, owner, 0).open(viewer);
            }
        });

        setItem(31, new ItemBuilder(Menus.PROFILE_HISTORIC_ITEMS_ICON.toMaterial())
                .name(Menus.PROFILE_HISTORIC_ITEMS_NAME.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .modelData(Menus.PROFILE_HISTORIC_ITEMS_MODEL_DATA.toInteger())
                .addLore(Menus.PROFILE_HISTORIC_ITEMS_LORE.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName()+"'s")).build(), e -> {
            if (viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.history")) {
                new HistoryMenu(viewer, owner, 0, null).open(viewer);
            } else if (viewer.getUniqueId() == owner.getUniqueId()) {
                new HistoryMenu(viewer, owner, 0, null).open(viewer);
            }
        });

        setItem(45, GuiHelper.constructButton(GuiButtonType.BACK), e -> new MainMenu(null, viewer, 0, null, null, null).open(viewer));
    }
}