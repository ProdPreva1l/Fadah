package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.ConfirmPurchaseMenu;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ViewListingCommand extends SubCommand {
    public ViewListingCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "view-listing", permission = "fadah.use", description = "View a specific listing.")
    public void execute(@NotNull SubCommandArguments command) {
        assert command.getPlayer() != null;
        if (command.args().length == 0) {
            command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.BAD_USAGE.toFormattedString("ah view-listing <uuid>")));
            return;
        }
        UUID listingId;
        try {
            listingId = UUID.fromString(command.args()[0]);
        } catch (IllegalArgumentException e) {
            command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + Lang.BAD_USAGE.toFormattedString("ah view-listing <uuid>")));
            return;
        }
        Listing listing = ListingCache.getListing(listingId);

        if (listing == null) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString());
            return;
        }

        if (listing.isOwner(command.getPlayer())) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.OWN_LISTING.toFormattedString());
            return;
        }

        new ConfirmPurchaseMenu(listing, command.getPlayer(), null, null, null,
                null, false, null).open(command.getPlayer());
    }
}
