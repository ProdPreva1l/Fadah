package info.preva1l.fadah.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.commands.subcommands.*;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.MenuManager;
import info.preva1l.fadah.utils.commands.Command;
import info.preva1l.fadah.utils.commands.CommandArgs;
import info.preva1l.fadah.utils.commands.CommandArguments;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.guis.LayoutManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AuctionHouseCommand extends Command {
    @Getter
    private static final List<SubCommand> subCommands = new ArrayList<>();

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
        subCommands.add(new ActiveListingsSubCommand(plugin));
        subCommands.add(new HistorySubCommand(plugin));
        subCommands.add(new ViewListingCommand(plugin));
        subCommands.add(new AboutSubCommand(plugin));
        subCommands.add(new ViewSubCommand(plugin));
    }

    @CommandArgs(name = "auctionhouse", aliases = {"ah", "auctions", "auction"}, inGameOnly = false, permission = "fadah.use")
    public void execute(@NotNull CommandArguments command) {
        if (command.args().length >= 1) {
            if (subCommandExecutor(command, subCommands)) return;
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.NO_COMMAND.toFormattedString());
            return;
        }
        if (!Fadah.getINSTANCE().getConfigFile().getBoolean("enabled")) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.AUCTION_DISABLED.toFormattedString());
            return;
        }
        if (command.getPlayer() == null) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.MUST_BE_PLAYER.toFormattedString());
            return;
        }

        MenuManager.getInstance().openMenu(command.getPlayer(), LayoutManager.MenuType.MAIN);
    }

    @Override
    public List<String> onTabComplete(CommandArguments command) {
        return subCommandsTabCompleter(command, subCommands);
    }
}
