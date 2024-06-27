package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProfileMenu extends FastInv {
    private final Player viewer;
    private final OfflinePlayer owner;

    public ProfileMenu(@NotNull Player viewer, @NotNull OfflinePlayer owner) {
        super(LayoutManager.MenuType.PROFILE.getLayout().guiSize(),
                LayoutManager.MenuType.PROFILE.getLayout().formattedTitle(viewer.getUniqueId() == owner.getUniqueId()
                ? Lang.WORD_YOUR.toCapital()
                : owner.getName()+"'s", owner.getName()+"'s"), LayoutManager.MenuType.PROFILE);
        this.viewer = viewer;
        this.owner = owner;

        List<Integer> fillerSlots = getLayout().fillerSlots();
        if (!fillerSlots.isEmpty()) {
            setItems(fillerSlots.stream().mapToInt(Integer::intValue).toArray(),
                    GuiHelper.constructButton(GuiButtonType.BORDER));
        }

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.BACK, 45),
                GuiHelper.constructButton(GuiButtonType.BACK),
                e -> new MainMenu(null, viewer, null, null, null).open(viewer));

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PROFILE_SUMMARY, 20),
                new ItemBuilder(Material.PLAYER_HEAD).skullOwner(owner)
                        .modelData(getLang().getInt("profile-button.model-data"))
                        .name(getLang().getStringFormatted("profile-button.name", "&e&l{0} Profile",
                                viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                        .addLore(getLang().getLore("profile-button.description",
                                List.of("&fThis is {0} profile!", "&fHere you will find items from the auction house relating to {0}."),
                                viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName(),
                                viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOU.toString() : owner.getName())).build());

        setButtons();
    }

    private void setButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PROFILE_ACTIVE_LISTINGS, 22),
                new ItemBuilder(getLang().getAsMaterial("your-listings.icon", Material.EMERALD))
                .name(getLang().getStringFormatted("your-listings.name", "&1Your listings", viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"))
                .modelData(getLang().getInt("your-listings.model-data"))
                .setAttributes(null)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .addLore(Menus.PROFILE_YOUR_LISTINGS_LORE.toLore(viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName()+"'s")).build(), e -> {
            if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.active-listings"))
                    || viewer.getUniqueId() == owner.getUniqueId()) {
                new ActiveListingsMenu(viewer, owner).open(viewer);
            }
        });
        
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PROFILE_COLLECTION_BOX, 23),
                new ItemBuilder(Menus.PROFILE_COLLECTION_BOX_ICON.toMaterial())
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

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PROFILE_EXPIRED_LISTINGS, 24),
                new ItemBuilder(Menus.PROFILE_EXPIRED_LISTINGS_ICON.toMaterial())
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

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PROFILE_HISTORY, 31),
                new ItemBuilder(Menus.PROFILE_HISTORIC_ITEMS_ICON.toMaterial())
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