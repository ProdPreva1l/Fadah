package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.guis.MenuManager;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import info.preva1l.fadah.utils.guis.LayoutManager;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class SellSubCommand extends SubCommand {
    public SellSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "sell", aliases = {"new-listing", "create-listing"}, permission = "fadah.use", description = "Create a new listing on the auction house!")
    public void execute(@NotNull SubCommandArguments command) {
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
        String priceString = command.args()[0];

        if (priceString.toLowerCase().contains("nan")) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.MUST_BE_NUMBER.toFormattedString());
            return;
        }

        int multi = 1;

        if (priceString.endsWith("k") || priceString.endsWith("K")) {
            multi = 1000;
            priceString = priceString.replace("k", "");
            priceString = priceString.replace("K", "");
        } else if (priceString.endsWith("m") || priceString.endsWith("M")) {
            multi = 1000000;
            priceString = priceString.replace("m", "");
            priceString = priceString.replace("M", "");
        }

        try {
            if (Double.parseDouble(priceString) * multi < Config.MIN_LISTING_PRICE.toDouble()) {
                command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.MIN_LISTING_PRICE.toFormattedString(Config.MIN_LISTING_PRICE.toString())));
                return;
            }
            if (Double.parseDouble(priceString) * multi > Config.MAX_LISTING_PRICE.toDouble()) {
                command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.MAX_LISTING_PRICE.toFormattedString(Config.MAX_LISTING_PRICE.toString())));
                return;
            }
            int currentListings = PermissionsData.getCurrentListings(command.getPlayer());
            int maxListings = PermissionsData.getHighestInt(PermissionsData.PermissionType.MAX_LISTINGS, command.getPlayer());
            if (currentListings >= maxListings) {
                command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.MAX_LISTINGS.toFormattedString(currentListings, maxListings)));
                return;
            }
            MenuManager.getInstance().openMenu(command.getPlayer(), LayoutManager.MenuType.NEW_LISTING,Double.parseDouble(priceString) * multi);
        } catch (NumberFormatException e) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.MUST_BE_NUMBER.toFormattedString());
        }
    }
}
