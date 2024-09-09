package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.ProfileMenu;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ProfileSubCommand extends SubCommand {
    public ProfileSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "profile", permission = "fadah.profile", description = "View your Auction Profile!")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Config.i().isEnabled()) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.AUCTION_DISABLED.toFormattedString());
            return;
        }
        assert command.getPlayer() != null;
        OfflinePlayer owner = command.getPlayer();
        if (command.args().length >= 1 && command.sender().hasPermission("fadah.manage.profiles")) {
            owner = Bukkit.getOfflinePlayer(command.args()[0]);
            Fadah.getINSTANCE().loadPlayerData(owner.getUniqueId());
        }
        if (owner.getUniqueId() != command.getPlayer().getUniqueId() && !HistoricItemsCache.playerExists(owner.getUniqueId())) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.PLAYER_NOT_FOUND.toFormattedString(command.args()[0]));
            return;
        }
        new ProfileMenu(command.getPlayer(), owner).open(command.getPlayer());
    }
}
