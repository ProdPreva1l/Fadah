package info.preva1l.fadah.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.commands.subcommands.*;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.MainMenu;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.Command;
import info.preva1l.fadah.utils.commands.CommandArgs;
import info.preva1l.fadah.utils.commands.CommandArguments;
import info.preva1l.fadah.utils.commands.SubCommand;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AuctionHouseCommand extends Command {
    @Getter private static final List<SubCommand> subCommands = new ArrayList<>();
    public AuctionHouseCommand(Fadah plugin) {
        super(plugin);
        subCommands.add(new DevSubCommand(plugin));
        subCommands.add(new ReloadSubCommand(plugin));
        subCommands.add(new SellSubCommand(plugin));
        subCommands.add(new ProfileSubCommand(plugin));
        subCommands.add(new CollectionBoxSubCommand(plugin));
        subCommands.add(new ToggleSubCommand(plugin));
        subCommands.add(new ExpiredItemsSubCommand(plugin));
        subCommands.add(new HelpSubCommand(plugin));
    }

    @CommandArgs(name = "auctionhouse", aliases = {"ah", "auctions", "auction"}, permission = "fadah.use")
    public void execute(@NotNull CommandArguments command) {
        assert command.getPlayer() != null;
        if (command.args().length >= 1) {
            if (subCommandExecutor(command, subCommands)) return;
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + StringUtils.colorize("&cThis command does not exist!"));
            return;
        }
        if (!Fadah.getINSTANCE().getConfigFile().getBoolean("enabled")) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + StringUtils.colorize("&cThe Auction House is currently disabled!"));
            return;
        }
        new MainMenu(null, command.getPlayer(), 0, null, null, null).open(command.getPlayer());
    }

    @Override
    public List<String> onTabComplete(CommandArguments command) {
        return subCommandsTabCompleter(command, subCommands);
    }
}
