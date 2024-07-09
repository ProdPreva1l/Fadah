package info.preva1l.fadah.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.migrator.Migrator;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.Command;
import info.preva1l.fadah.utils.commands.CommandArgs;
import info.preva1l.fadah.utils.commands.CommandArguments;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MigrateCommand extends Command {
    public MigrateCommand(Fadah plugin) {
        super(plugin);
    }

    @CommandArgs(name = "fadah-migrate", permission = "fadah.migrate", inGameOnly = false)
    public void execute(@NotNull CommandArguments command) {
        if (command.args().length == 0) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + StringUtils.colorize("&cUsage: &f/fadah-migrate <plugin>"));
            return;
        }

        if (!plugin.getMigratorManager().migratorExists(command.args()[0])) {
            command.sender().sendMessage(Lang.PREFIX.toFormattedString() + StringUtils.colorize("&cMigrator does not exist!"));
            return;
        }

        Migrator migrator = plugin.getMigratorManager().getMigrator(command.args()[0]);
        assert migrator != null;

        long start = Instant.now().toEpochMilli();
        Fadah.getConsole().info("Starting migration from %s...".formatted(migrator.getMigratorName()));
        migrator.startMigration(plugin).thenRun(() -> {
            Fadah.getConsole().info("Migration from %s complete! (Took: %sms)"
                    .formatted(migrator.getMigratorName(), Instant.now().toEpochMilli() - start));
        });
    }

    @Override
    public List<String> onTabComplete(CommandArguments command) {
        List<String> completions = new ArrayList<>();
        if (command.args().length != 1) return completions;
        StringUtil.copyPartialMatches(command.args()[0], plugin.getMigratorManager().getMigratorNames(), completions);
        return completions;
    }
}
