package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.commands.AuctionHouseCommand;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;

public class HelpSubCommand extends SubCommand {
    public HelpSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getHelp().getAliases(), Lang.i().getCommands().getHelp().getDescription());
    }

    @SubCommandArgs(name = "help", permission = "fadah.help", inGameOnly = false)
    public void execute(@NotNull SubCommandArguments command) {
        StringBuilder message = new StringBuilder(Lang.i().getCommands().getHelp().getHeader());
        for (SubCommand subCommand : AuctionHouseCommand.getSubCommands()) {
            if (!command.sender().hasPermission(subCommand.getAssigned().permission())) continue;
            message.append("\n").append(Lang.i().getCommands().getHelp().getDescription()
                    .replace("%command%", subCommand.getAssigned().name())
                    .replace("%description%", subCommand.getDescription()));
        }
        command.reply(message.toString());
    }
}
