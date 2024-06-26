package info.preva1l.fadah.utils.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.CommandMapUtil;
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
        CommandMapUtil.getCommandMap(plugin.getServer());
        CommandMapUtil.getCommandMap(plugin.getServer()).register(plugin.getDescription().getName().toLowerCase(), command);
        loadedCommands.add(base);
    }
}