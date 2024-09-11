package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.commands.AuctionHouseCommand;
import info.preva1l.fadah.config.old.Lang;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;

public class HelpSubCommand extends SubCommand {
    public HelpSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "help", permission = "fadah.help", inGameOnly = false, description = "This Command!")
    public void execute(@NotNull SubCommandArguments command) {
        StringBuilder message = new StringBuilder(Lang.HELP_COMMAND_HEADER.toFormattedString());
        for (SubCommand subCommand : AuctionHouseCommand.getSubCommands()) {
            if (!command.sender().hasPermission(subCommand.getAssigned().permission())) continue;
            message.append("\n").append(Lang.HELP_COMMAND_FORMAT.toFormattedString("ah " + subCommand.getAssigned().name(), subCommand.getAssigned().description()));
        }
        command.sender().sendMessage(message.toString());
    }
}
