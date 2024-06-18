package info.preva1l.fadah.commands.subcommands;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class DevSubCommand extends SubCommand {
    public DevSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "dev", permission = "fadah.developer", description = "A Basic Debug Command for testing (useless to the average user)")
    public void execute(@NotNull SubCommandArguments command) {
        ItemStack itemStack = new ItemStack(Material.WHEAT);
        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(List.of(Component.text("Test", TextColor.color(95, 237, 88))));
        itemStack.setItemMeta(meta);
        MultiLib.getEntityScheduler(command.getPlayer()).execute(Fadah.getINSTANCE() ,
                () -> command.getPlayer().getInventory().addItem(itemStack), null, 0L);
    }
}
