package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.NewListingMenu;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class SellSubCommand extends SubCommand {

    public SellSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "sell", aliases = {"new-listing", "create-listing"}, permission = "fadah.use")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Fadah.getINSTANCE().getConfigFile().getBoolean("enabled")) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + StringUtils.colorize("&cThe Auction House is currently disabled!"));
            return;
        }
        assert command.getPlayer() != null;
        if (command.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + StringUtils.colorize("&fYou must have an item in your hand to sell!"));
            return;
        }
        if (command.args().length == 0) {
            command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + "&cUsage: /ah sell <price>"));
            return;
        }
        if (Double.parseDouble(command.args()[0]) < 1) {
            command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + "&fPrice must be at least &a$1"));
            return;
        }
        if (Double.parseDouble(command.args()[0]) > Config.MAX_LISTING_PRICE.toDouble()) {
            command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + "&fPrice must be less than $1,000,000,000"));
            return;
        }
        new NewListingMenu(command.getPlayer(), Double.parseDouble(command.args()[0])).open(command.getPlayer());
    }
}
