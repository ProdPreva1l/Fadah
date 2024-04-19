package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.GuiButtonType;
import info.preva1l.fadah.utils.guis.GuiHelper;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProfileMenu extends FastInv {
    public ProfileMenu(@NotNull Player player) {
        super(45, Menus.PROFILE_TITLE.toFormattedString());
        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        setItem(20, new ItemBuilder(Material.PLAYER_HEAD).skullOwner(player)
                .name(Menus.MAIN_PROFILE_NAME.toFormattedString())
                .addLore(Menus.MAIN_PROFILE_DESCRIPTION.toLore()).build());
        setItem(22, new ItemBuilder(Menus.PROFILE_YOUR_LISTINGS_ICON.toMaterial())
                .name(Menus.PROFILE_YOUR_LISTINGS_NAME.toFormattedString())
                .addLore(Menus.PROFILE_YOUR_LISTINGS_LORE.toLore()).build(), e -> new ActiveListingsMenu(player, 0).open(player));
        setItem(23, new ItemBuilder(Menus.PROFILE_COLLECTION_BOX_ICON.toMaterial())
                .name(Menus.PROFILE_COLLECTION_BOX_NAME.toFormattedString())
                .addLore(Menus.PROFILE_COLLECTION_BOX_LORE.toLore()).build(), e -> new CollectionBoxMenu(player, 0).open(player));
        setItem(24, new ItemBuilder(Menus.PROFILE_EXPIRED_LISTINGS_ICON.toMaterial())
                .name(Menus.PROFILE_EXPIRED_LISTINGS_NAME.toFormattedString())
                .addLore(Menus.PROFILE_EXPIRED_LISTINGS_LORE.toLore()).build(), e -> new ExpiredListingsMenu(player, 0).open(player));

        setItem(36, GuiHelper.constructButton(GuiButtonType.BACK), e -> new MainMenu(null, player, 0, null, null, null).open(player));
    }
}