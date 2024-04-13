package info.preva1l.fadah.guis;


import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.utils.StringUtils;
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
        setItems(getBorders(),
                GuiHelper.constructButton(GuiButtonType.GENERIC, Material.BLACK_STAINED_GLASS_PANE,
                        StringUtils.colorize("&r "), Menus.BORDER_LORE.toLore()));

        setItem(20, new ItemBuilder(Material.PLAYER_HEAD).skullOwner(player)
                .name(Menus.PROFILE_COLLECTION_BOX_NAME.toFormattedString())
                .addLore(Menus.PROFILE_COLLECTION_BOX_LORE.toLore()).build());

        setItem(22, new ItemBuilder(Material.CHEST_MINECART)
                .name(Menus.PROFILE_COLLECTION_BOX_NAME.toFormattedString())
                .addLore(Menus.PROFILE_COLLECTION_BOX_LORE.toLore()).build(),e -> new CollectionBoxMenu(player, 0).open(player));
        setItem(24, new ItemBuilder(Material.ENDER_CHEST)
                .name(Menus.PROFILE_EXPIRED_LISTINGS_NAME.toFormattedString())
                .addLore(Menus.PROFILE_EXPIRED_LISTINGS_LORE.toLore()).build(),e -> new ExpiredListingsMenu(player, 0).open(player));

        setItem(36, GuiHelper.constructButton(GuiButtonType.BACK),e->new MainMenu(null, player, 0, null, null, null).open(player));
    }
}