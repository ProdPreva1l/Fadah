package info.preva1l.fadah.utils.commands;

import info.preva1l.fadah.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public record CommandArguments(CommandSender sender, String label, String[] args) {
    public Player getPlayer() {
        if (!(sender instanceof Player)) return null;
        return (Player) sender;
    }

    public void reply(String message) {
        sender.sendMessage(StringUtils.colorize(message));
    }
}