package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.ProfileMenu;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;

public class ProfileSubCommand extends SubCommand {
    public ProfileSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "profile", permission = "fadah.profile")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Fadah.getINSTANCE().getConfigFile().getBoolean("enabled")) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + StringUtils.colorize("&cThe Auction House is currently disabled!"));
            return;
        }
        assert command.getPlayer() != null;
        new ProfileMenu(command.getPlayer()).open(command.getPlayer());
    }
}
