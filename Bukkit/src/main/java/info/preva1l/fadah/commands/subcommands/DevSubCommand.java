package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;


public class DevSubCommand extends SubCommand {
    public DevSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "dev", aliases = {"dev-tools"}, permission = "fadah.developer", description = "A Basic Debug Command for testing")
    public void execute(@NotNull SubCommandArguments command) {
        for (Category category : CategoryCache.getCategories()) {
            String string = "ID: {0}<newline>" +
                    "Name: {1}<newline>" +
                    "Description: {2}<newline>" +
                    "Materials: {3}<newline>" +
                    "Is Custom Items: {4}<newline>" +
                    "Custom Item IDs: {5}";

            command.sender().sendMessage(StringUtils.message(string,
                    category.id(), category.name(),
                    category.description(), category.materials(),
                    category.isCustomItems(), category.customItemIds()));
        }
    }
}
