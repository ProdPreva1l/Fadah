package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import info.preva1l.fadah.utils.guis.FastInvManager;
import org.jetbrains.annotations.NotNull;

public class ToggleSubCommand extends SubCommand {
    public ToggleSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "toggle", inGameOnly = false, permission = "fadah.toggle-status", description = "Toggles the auction house on or off.")
    public void execute(@NotNull SubCommandArguments command) {
        if (Fadah.getINSTANCE().getCacheSync() != null) {
            CacheSync.send(CacheSync.CacheType.TOGGLE);
            return;
        }
        boolean enabled = Fadah.getINSTANCE().getConfigFile().getBoolean("enabled");
        Fadah.getINSTANCE().getConfigFile().getConfiguration().set("enabled", !enabled);

        String message = Lang.PREFIX.toFormattedString() + StringUtils.colorize("&fAuction House has been ");
        message += (enabled ? StringUtils.colorize("&c&lDisabled!") : StringUtils.colorize("&a&lEnabled!"));

        Fadah.getINSTANCE().getConfigFile().save();
        FastInvManager.closeAll();

        command.sender().sendMessage(message);
    }
}
