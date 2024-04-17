package info.preva1l.fadah.commands.subcommands;


import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.CollectionBoxMenu;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
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
        new CollectionBoxMenu(command.getPlayer(), 0).open(command.getPlayer());
    }
}
