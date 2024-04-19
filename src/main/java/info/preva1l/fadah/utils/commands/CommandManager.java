package info.preva1l.fadah.utils.commands;

import info.preva1l.fadah.Fadah;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommandManager {

    private final Fadah plugin;
    private final List<Command> loadedCommands = new ArrayList<>();

    public CommandManager(Fadah plugin) {
        this.plugin = plugin;
    }

    public void registerCommand(Command base, Command.CommandExecutor command) {
        plugin.getServer().getCommandMap();
        plugin.getServer().getCommandMap().register(plugin.getDescription().getName().toLowerCase(), command);
        loadedCommands.add(base);
    }

    public void registerCommand(Command base, Command.CommandExecutor command, List<String> aliases) {
        if (base.getAssigned() == null) return;

        command.getAliases().addAll(aliases);

        plugin.getServer().getCommandMap();
        plugin.getServer().getCommandMap().register(plugin.getDescription().getName().toLowerCase(), command);

        loadedCommands.removeIf(loaded -> loaded.getAssigned().name().equalsIgnoreCase(base.getAssigned().name()));
        loadedCommands.add(base);
    }
}