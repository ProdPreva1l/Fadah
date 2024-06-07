package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.PermissionsData;
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

    @SubCommandArgs(name = "sell", aliases = {"new-listing", "create-listing"}, permission = "fadah.use", description = "Create a new listing on the auction house!")
    public void execute(@NotNull SubCommandArguments command) {
        try {
            if (!Fadah.getINSTANCE().getConfigFile().getBoolean("enabled")) {
                command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.AUCTION_DISABLED.toFormattedString());
                return;
            }
            assert command.getPlayer() != null;
            if (command.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
                command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.MUST_HOLD_ITEM.toFormattedString());
                return;
            }
            if (command.args().length == 0) {
                command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.BAD_USAGE.toFormattedString("ah sell <price>")));
                return;
            }
            if (Double.parseDouble(command.args()[0]) < Config.MIN_LISTING_PRICE.toDouble()) {
                command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.MIN_LISTING_PRICE.toFormattedString(Config.MIN_LISTING_PRICE.toString())));
                return;
            }
            if (Double.parseDouble(command.args()[0]) > Config.MAX_LISTING_PRICE.toDouble()) {
                command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.MAX_LISTING_PRICE.toFormattedString(Config.MAX_LISTING_PRICE.toString())));
                return;
            }
            int currentListings = PermissionsData.getCurrentListings(command.getPlayer());
            int maxListings = PermissionsData.valueFromPermission(PermissionsData.PermissionType.MAX_LISTINGS, command.getPlayer());
            if (currentListings >= maxListings) {
                command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.MAX_LISTINGS.toFormattedString(currentListings, maxListings)));
                return;
            }
            new NewListingMenu(command.getPlayer(), Double.parseDouble(command.args()[0])).open(command.getPlayer());
        } catch (NumberFormatException e) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.MUST_BE_NUMBER.toFormattedString());
        }
    }
}
