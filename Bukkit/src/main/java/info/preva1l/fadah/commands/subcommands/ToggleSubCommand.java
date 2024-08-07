package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.multiserver.Message;
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
        if (Fadah.getINSTANCE().getBroker() != null) {
            Message.builder().type(Message.Type.TOGGLE).build().send(Fadah.getINSTANCE().getBroker());
            return;
        }
        FastInvManager.closeAll(plugin);
        boolean enabled = Fadah.getINSTANCE().getConfigFile().getBoolean("enabled");
        Fadah.getINSTANCE().getConfigFile().save();
        Fadah.getINSTANCE().getConfigFile().getConfiguration().set("enabled", !enabled);
        Fadah.getINSTANCE().getConfigFile().save();
        Fadah.getINSTANCE().getConfigFile().load();

        String toggle = enabled ? Lang.ADMIN_TOGGLE_DISABLED.toFormattedString() : Lang.ADMIN_TOGGLE_ENABLED.toFormattedString();
        command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.ADMIN_TOGGLE_MESSAGE.toFormattedString(toggle));
    }
}
