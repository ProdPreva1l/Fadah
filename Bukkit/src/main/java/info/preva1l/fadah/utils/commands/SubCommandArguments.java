package info.preva1l.fadah.utils.commands;

import info.preva1l.fadah.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public record SubCommandArguments(CommandSender sender, String label, String[] args, boolean async, boolean inGameOnly) {
    public Player getPlayer() {
        if (inGameOnly) return (Player) sender;
        if (!(sender instanceof Player)) return null;
        return (Player) sender;
    }

    public void reply(String message) {
        sender.sendMessage(StringUtils.colorize(message));
    }
}