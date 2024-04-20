package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.ActiveListingsMenu;
import info.preva1l.fadah.guis.CollectionBoxMenu;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class CollectionBoxSubCommand extends SubCommand {
    public CollectionBoxSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "collectionbox", aliases = {"collection-box", "redeem"}, permission = "fadah.collection-box", description = "View your Collection Box!")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Fadah.getINSTANCE().getConfigFile().getBoolean("enabled")) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.AUCTION_DISABLED.toFormattedString());
            return;
        }
        assert command.getPlayer() != null;
        OfflinePlayer owner = command.getPlayer();
        if (command.args().length >= 1 && command.sender().hasPermission("fadah.manage.collection-box")) {
            owner = Bukkit.getOfflinePlayer(command.args()[0]);
            Fadah.getINSTANCE().getDatabase().loadCollectionBox(owner.getUniqueId());
        }
        if (!ListingCache.playerHasListings(owner.getUniqueId())
                && !ExpiredListingsCache.playerHasExpiredListings(owner.getUniqueId())
                && !CollectionBoxCache.playerHasCollectableItems(owner.getUniqueId())) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.PLAYER_NOT_FOUND.toFormattedString(command.args()[0]));
            return;
        }
        new CollectionBoxMenu(command.getPlayer(), owner, 0).open(command.getPlayer());
    }
}
