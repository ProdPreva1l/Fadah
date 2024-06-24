package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

public class ProfileMenu extends FastInv {
    private final Player viewer;
    private final OfflinePlayer owner;

    public ProfileMenu(@NotNull Player viewer, @NotNull OfflinePlayer owner) {
        super(54, Menus.PROFILE_TITLE.toFormattedString(viewer.getUniqueId() == owner.getUniqueId()
                ? Lang.WORD_YOUR.toCapital()
                : owner.getName()+"'s", owner.getName()+"'s"), LayoutManager.MenuType.PROFILE);
        this.viewer = viewer;
        this.owner = owner;

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));
        setItem(45, GuiHelper.constructButton(GuiButtonType.BACK), e -> new MainMenu(null, viewer, null, null, null).open(viewer));
        fillItems();
    }

    private void fillItems() {
        // TODO: dont use main menu stuff
        setItem(20, new ItemBuilder(Material.PLAYER_HEAD).skullOwner(owner)
                .name(Menus.MAIN_FILTER_DIRECTION_NAME.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .addLore(Menus.MAIN_PROFILE_DESCRIPTION.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName(),
                        viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOU.toString() : owner.getName())).build());

        setItem(22, new ItemBuilder(Menus.PROFILE_YOUR_LISTINGS_ICON.toMaterial())
                .name(Menus.PROFILE_YOUR_LISTINGS_NAME.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .modelData(Menus.PROFILE_YOUR_LISTINGS_MODEL_DATA.toInteger())
                .setAttributes(null)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .addLore(Menus.PROFILE_YOUR_LISTINGS_LORE.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName()+"'s")).build(), e -> {
            if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.active-listings"))
                    || viewer.getUniqueId() == owner.getUniqueId()) {
                new ActiveListingsMenu(viewer, owner).open(viewer);
            }
        });
        
        setItem(23, new ItemBuilder(Menus.PROFILE_COLLECTION_BOX_ICON.toMaterial())
                .name(Menus.PROFILE_COLLECTION_BOX_NAME.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .modelData(Menus.PROFILE_COLLECTION_BOX_MODEL_DATA.toInteger())
                .setAttributes(null)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .addLore(Menus.PROFILE_COLLECTION_BOX_LORE.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName()+"'s")).build(), e -> {
            if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.collection-box"))
                    || viewer.getUniqueId() == owner.getUniqueId()) {
                new CollectionBoxMenu(viewer, owner).open(viewer);
            }
        });

        setItem(24, new ItemBuilder(Menus.PROFILE_EXPIRED_LISTINGS_ICON.toMaterial())
                .name(Menus.PROFILE_EXPIRED_LISTINGS_NAME.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .modelData(Menus.PROFILE_EXPIRED_LISTINGS_MODEL_DATA.toInteger())
                .setAttributes(null)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .addLore(Menus.PROFILE_EXPIRED_LISTINGS_LORE.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName()+"'s")).build(), e -> {
            if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.expired-listings"))
                    || viewer.getUniqueId() == owner.getUniqueId()) {
                new ExpiredListingsMenu(viewer, owner, 0).open(viewer);
            }
        });

        setItem(31, new ItemBuilder(Menus.PROFILE_HISTORIC_ITEMS_ICON.toMaterial())
                .name(Menus.PROFILE_HISTORIC_ITEMS_NAME.toFormattedString(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .modelData(Menus.PROFILE_HISTORIC_ITEMS_MODEL_DATA.toInteger())
                .setAttributes(null)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .addLore(Menus.PROFILE_HISTORIC_ITEMS_LORE.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName()+"'s")).build(), e -> {
            if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.history"))
                    || viewer.getUniqueId() == owner.getUniqueId()) {
                new HistoryMenu(viewer, owner, null).open(viewer);
            }
        });
    }
}