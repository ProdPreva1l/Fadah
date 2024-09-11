package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.old.Lang;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;

public class ReloadSubCommand extends SubCommand {
    public ReloadSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "reload", aliases = {"rl"}, permission = "fadah.reload", inGameOnly = false, description = "Reloads the plugin!")
    public void execute(@NotNull SubCommandArguments command) {
        if (Fadah.getINSTANCE().getBroker() != null) {
            Message.builder().type(Message.Type.RELOAD).build().send(Fadah.getINSTANCE().getBroker());
            return;
        }
        plugin.reload();
        command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.ADMIN_RELOAD.toFormattedString());
    }
}
