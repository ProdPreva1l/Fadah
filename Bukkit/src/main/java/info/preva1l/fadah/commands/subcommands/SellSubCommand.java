package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.guis.NewListingMenu;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class SellSubCommand extends SubCommand {
    public SellSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getSell().getAliases(), Lang.i().getCommands().getSell().getDescription());
    }

    @SubCommandArgs(name = "sell", permission = "fadah.use")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        assert command.getPlayer() != null;
        if (command.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustHoldItem());
            return;
        }
        if (command.args().length == 0) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getInvalidUsage()
                    .replace("%command%", Lang.i().getCommands().getSell().getUsage()));
            return;
        }
        String priceString = command.args()[0];

        if (priceString.toLowerCase().contains("nan")) {
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustBeNumber());
            return;
        }

        int multi = 1;

        if (priceString.toLowerCase().endsWith("k")) {
            multi = 1000;
            priceString = priceString.replace("k", "");
            priceString = priceString.replace("K", "");
        } else if (priceString.toLowerCase().endsWith("m")) {
            multi = 1000000;
            priceString = priceString.replace("m", "");
            priceString = priceString.replace("M", "");
        } else if (priceString.toLowerCase().endsWith("b")) {
            multi = 1000000000;
            priceString = priceString.replace("b", "");
            priceString = priceString.replace("B", "");
        }

        try {
            if (Double.parseDouble(priceString) * multi < Config.i().getListingPrice().getMin()) {
                command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getListingPrice().getMin()
                        .replace("%price%", Config.i().getListingPrice().getMin() + ""));
                return;
            }
            if (Double.parseDouble(priceString) * multi > Config.i().getListingPrice().getMax()) {
                command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getListingPrice().getMax()
                        .replace("%price%", Config.i().getListingPrice().getMax() + ""));
                return;
            }
            int currentListings = PermissionsData.getCurrentListings(command.getPlayer());
            int maxListings = PermissionsData.getHighestInt(PermissionsData.PermissionType.MAX_LISTINGS, command.getPlayer());
            if (currentListings >= maxListings) {
                command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMaxListings()
                        .replace("%current%", currentListings + "")
                        .replace("%max%", maxListings + ""));
                return;
            }
            new NewListingMenu(command.getPlayer(), Double.parseDouble(priceString) * multi).open(command.getPlayer());
        } catch (NumberFormatException e) {
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustBeNumber());
        }
    }
}
