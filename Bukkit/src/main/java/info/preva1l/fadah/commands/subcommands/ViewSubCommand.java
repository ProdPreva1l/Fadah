package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.java.ViewListingsMenu;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ViewSubCommand extends SubCommand {
    public ViewSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "view", aliases = "visit", permission = "fadah.view", description = "View another players active listings")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Fadah.getINSTANCE().getConfigFile().getBoolean("enabled")) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.AUCTION_DISABLED.toFormattedString());
            return;
        }
        if (command.args().length == 0) {
            command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.BAD_USAGE.toFormattedString("ah view <player>")));
            return;
        }
        assert command.getPlayer() != null;
        OfflinePlayer owner = Bukkit.getOfflinePlayer(command.args()[0]);
        if (owner.getUniqueId() != command.getPlayer().getUniqueId() && !HistoricItemsCache.playerExists(owner.getUniqueId())) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.PLAYER_NOT_FOUND.toFormattedString(command.args()[0]));
            return;
        }
        new ViewListingsMenu(command.getPlayer(), owner).open(command.getPlayer());
    }
}
