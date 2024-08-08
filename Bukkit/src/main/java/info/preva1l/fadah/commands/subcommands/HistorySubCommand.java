package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.guis.java.ExpiredListingsMenu;
import info.preva1l.fadah.records.History;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class HistorySubCommand extends SubCommand {
    public HistorySubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "history", aliases = {"expireditems", "expired"}, permission = "fadah.expired-items", description = "View your Expired Items!")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Fadah.getINSTANCE().getConfigFile().getBoolean("enabled")) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.AUCTION_DISABLED.toFormattedString());
            return;
        }
        assert command.getPlayer() != null;
        OfflinePlayer owner = command.getPlayer();
        if (command.args().length >= 1 && command.sender().hasPermission("fadah.manage.history")) {
            owner = Bukkit.getOfflinePlayer(command.args()[0]);
            final OfflinePlayer finalOwner = owner;
            DatabaseManager.getInstance().get(History.class, owner.getUniqueId())
                    .thenAccept(history -> history.ifPresent(items -> HistoricItemsCache.update(finalOwner.getUniqueId(), items.collectableItems())));
        }
        if (owner.getUniqueId() != command.getPlayer().getUniqueId() && !HistoricItemsCache.playerExists(owner.getUniqueId())) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.PLAYER_NOT_FOUND.toFormattedString(command.args()[0]));
            return;
        }
        new ExpiredListingsMenu(command.getPlayer(), owner, 0).open(command.getPlayer());
    }
}
