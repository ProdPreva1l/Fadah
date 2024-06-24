package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;

public class DevSubCommand extends SubCommand {
    public DevSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "dev", permission = "fadah.developer", description = "A Basic Debug Command for testing (useless to the average user)")
    public void execute(@NotNull SubCommandArguments command) {
        // Do nothing
    }
}
