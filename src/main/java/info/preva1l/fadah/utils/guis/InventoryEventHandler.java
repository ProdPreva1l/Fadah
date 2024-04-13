package info.preva1l.fadah.utils.guis;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class InventoryEventHandler {
    public final List<SearchInv> inventoriesToHandle = new ArrayList<>();
    public final Map<Inventory, BukkitTask> tasksToQuit = new HashMap<>();
}
