package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Lang;
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
                        .addLore(getLang().getLore("profile-button.lore",
                                List.of("&fThis is {0} profile!", "&fHere you will find items from the auction house relating to {1}."),
                                viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName(),
                                viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOU.toString() : owner.getName(),  owner.getName())).build());

        activeListingsButton();
        collectionBoxButton();
        expiredListingsButton();
        historyButton();
    }

    private void activeListingsButton() {
        List<String> defLore = List.of(
                "&fClick to view & manage",
                "&f{0} active listings!"
        );
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PROFILE_ACTIVE_LISTINGS, 22),
                new ItemBuilder(getLang().getAsMaterial("your-listings.icon", Material.EMERALD))
                        .name(getLang().getStringFormatted("your-listings.name", "&1{0} listings", viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName() + "'s", owner.getName() + "'s"))
                        .modelData(getLang().getInt("your-listings.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(getLang().getLore("your-listings.lore", defLore, viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName() + "'s")).build(), e -> {
                    if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.active-listings"))
                            || viewer.getUniqueId() == owner.getUniqueId()) {
                        new ActiveListingsMenu(viewer, owner).open(viewer);
                    }
                });
    }

    private void collectionBoxButton() {
        List<String> defLore = List.of(
                "&fClick to view & claim",
                "&f{0} purchases!"
        );
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PROFILE_COLLECTION_BOX, 23),
                new ItemBuilder(getLang().getAsMaterial("collection-box.icon", Material.CHEST_MINECART))
                        .name(getLang().getStringFormatted("collection-box.name", "&e{0} Collection Box", viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName() + "'s", owner.getName() + "'s"))
                        .modelData(getLang().getInt("collection-box.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(getLang().getLore("collection-box.lore", defLore, viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName() + "'s")).build(), e -> {
                    if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.collection-box"))
                            || viewer.getUniqueId() == owner.getUniqueId()) {
                        new CollectionBoxMenu(viewer, owner).open(viewer);
                    }
                });
    }

    private void expiredListingsButton() {
        List<String> defLore = List.of(
                "&fClick to view & claim",
                "&f{0} expired listings!"
        );
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PROFILE_EXPIRED_LISTINGS, 24),
                new ItemBuilder(getLang().getAsMaterial("expired-items.icon", Material.ENDER_CHEST))
                        .name(getLang().getStringFormatted("expired-items.name", "&c{0} Expired Listings", viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName() + "'s", owner.getName() + "'s"))
                        .modelData(getLang().getInt("expired-items.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(getLang().getLore("your-listings.lore", defLore, viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName() + "'s")).build(), e -> {
                    if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.expired-listings"))
                            || viewer.getUniqueId() == owner.getUniqueId()) {
                        new ExpiredListingsMenu(viewer, owner, 0).open(viewer);
                    }
                });
    }

    private void historyButton() {
        List<String> defLore = List.of(
                "&fClick to view",
                "&f{0} history!"
        );
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PROFILE_HISTORY, 31),
                new ItemBuilder(getLang().getAsMaterial("historic-items.icon", Material.ENDER_CHEST))
                        .name(getLang().getStringFormatted("historic-items.name", "&c{0} History", viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toCapital() : owner.getName() + "'s", owner.getName() + "'s"))
                        .modelData(getLang().getInt("historic-items.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(getLang().getLore("your-listings.lore", defLore, viewer.getUniqueId() == owner.getUniqueId() ? Lang.WORD_YOUR.toString() : owner.getName() + "'s")).build(), e -> {
                    if ((viewer.getUniqueId() != owner.getUniqueId() && viewer.hasPermission("fadah.manage.history"))
                            || viewer.getUniqueId() == owner.getUniqueId()) {
                        new HistoryMenu(viewer, owner, null).open(viewer);
                    }
                });
    }
}