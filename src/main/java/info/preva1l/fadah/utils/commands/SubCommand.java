package info.preva1l.fadah.utils.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.TaskManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class SubCommand {

    private final SubCommandArgs assigned;
    public Fadah plugin;
    private SubCommandArguments executeArguments;
    private boolean senderHasPermission = false;

    public SubCommand(Fadah plugin) {
        this.plugin = plugin;
        this.assigned = Arrays.stream(this.getClass().getMethods()).filter(method -> method.getAnnotation(SubCommandArgs.class) != null).map(method -> method.getAnnotation(SubCommandArgs.class)).findFirst().orElse(null);
    }

    public abstract void execute(@NotNull SubCommandArguments command);

    public final void executor(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (this.assigned.inGameOnly() && sender instanceof ConsoleCommandSender) {
            sender.sendMessage(Lang.PREFIX.toFormattedString() + Lang.MUST_BE_PLAYER.toFormattedString());
            return;
        }
        if (this.assigned.permission() != null && !sender.hasPermission(this.assigned.permission())) {
            senderHasPermission = false;
            sender.sendMessage(Lang.PREFIX.toFormattedString() + Lang.NO_PERMISSION.toFormattedString());
            return;
        }

        if (args.length == 0) {
            this.executeArguments = new SubCommandArguments(sender, label, new String[0], this.assigned.async(), this.assigned.inGameOnly());
        } else {
            this.executeArguments = new SubCommandArguments(sender, label, Arrays.copyOfRange(args, 1, args.length), this.assigned.async(), this.assigned.inGameOnly());
        }

        if (this.assigned.async()) {
            TaskManager.Async.run(plugin, () -> this.execute(executeArguments));
        } else {
            TaskManager.Sync.run(plugin, () -> this.execute(executeArguments));
        }
    }

    public List<String> onTabComplete(SubCommandArguments command) {
        return new ArrayList<>();
    }

    //This is default tab complete which should return a list of online players
    public List<String> getDefaultTabComplete(CommandArguments command) {
        List<String> completors = new ArrayList<>();

        List<String> values = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();

        String[] args = command.args();

        if (args.length == 0) return new ArrayList<>();

        if (!args[args.length - 1].equalsIgnoreCase("")) {
            values.forEach(value -> {
                if (value.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                    completors.add(value);
                }
            });
        } else {
            completors.addAll(values);
        }
        return completors;
    }
}
