package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.guis.SearchMenu;
import org.bukkit.inventory.InventoryView;

public record SearchInv(InventoryView view, SearchMenu.SearchCallback callback) {
}
