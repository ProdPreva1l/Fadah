package info.preva1l.fadah.commands.subcommands;


import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.guis.java.ExpiredListingsMenu;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ExpiredItemsSubCommand extends SubCommand {
    public ExpiredItemsSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "expired-items", aliases = {"expireditems", "expired"}, permission = "fadah.expired-items", description = "View your Expired Items!")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Fadah.getINSTANCE().getConfigFile().getBoolean("enabled")) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.AUCTION_DISABLED.toFormattedString());
            return;
        }
        assert command.getPlayer() != null;
        OfflinePlayer owner = command.getPlayer();
        if (command.args().length >= 1 && command.sender().hasPermission("fadah.manage.expired-items")) {
            owner = Bukkit.getOfflinePlayer(command.args()[0]);
            final OfflinePlayer finalOwner = owner;
            DatabaseManager.getInstance().get(ExpiredItems.class, finalOwner.getUniqueId())
                    .thenAccept(var1 -> var1.ifPresent(list -> ExpiredListingsCache.update(finalOwner.getUniqueId(), list.collectableItems())));
        }
        if (owner.getUniqueId() != command.getPlayer().getUniqueId() && !HistoricItemsCache.playerExists(owner.getUniqueId())) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.PLAYER_NOT_FOUND.toFormattedString(command.args()[0]));
            return;
        }
        new ExpiredListingsMenu(command.getPlayer(), owner, 0).open(command.getPlayer());
    }
}
