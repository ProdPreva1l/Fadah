package info.preva1l.fadah.guis;

import info.preva1l.fadah.utils.guis.InventoryEventHandler;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.SearchInv;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;

public class SearchMenu implements Listener {

    public SearchMenu(Player player, String placeholder, SearchCallback callback) {
        InventoryView view = player.openAnvil(null, true);
        if (view == null) return;
        InventoryEventHandler.inventoriesToHandle.add(new SearchInv(view, callback));
        ((AnvilInventory) view.getTopInventory()).setMaximumRepairCost(0);
        ((AnvilInventory) view.getTopInventory()).setRepairCost(0);
        ((AnvilInventory) view.getTopInventory()).setFirstItem(new ItemBuilder(Material.PAPER).name(placeholder).build());
    }

    @FunctionalInterface
    public interface SearchCallback {
        /**
         * Returns the search query
         *
         * @param query the search query
         */
        void search(String query);
    }
}
