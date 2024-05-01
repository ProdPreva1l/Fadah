package info.preva1l.fadah.listeners;


import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.guis.InventoryEventHandler;
import info.preva1l.fadah.utils.guis.SearchInv;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class PlayerListener implements Listener {
    @EventHandler
    @SuppressWarnings("deprecation")
    public void joinListener(AsyncPlayerPreLoginEvent e) {
        if (!Fadah.getINSTANCE().getDatabase().isConnected()) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(Lang.PREFIX.toFormattedString() + Lang.DATABASE_CONNECTING.toFormattedString());
            return;
        }

        Fadah.getINSTANCE().getDatabase().loadPlayerData(e.getUniqueId()).thenAccept((success) -> {
            if (!success) {
                e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                e.setKickMessage(Lang.PREFIX.toFormattedString() + Lang.DATABASE_CONNECTING.toFormattedString());
            }
        });
    }

    @EventHandler
    public void leaveListener(PlayerQuitEvent e) {
        CollectionBoxCache.purgeCollectionbox(e.getPlayer().getUniqueId());
        ExpiredListingsCache.purgeExpiredListings(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        for (SearchInv inv : List.copyOf(InventoryEventHandler.inventoriesToHandle)) {
            if (event.getView() != inv.view()) {
                return;
            }
            event.setCancelled(true);
            if (event.getClickedInventory() != inv.view().getTopInventory()) return;
            if (event.getSlot() != 2 || event.getCurrentItem() == null) return;
            String search = ((AnvilInventory) inv.view().getTopInventory()).getRenameText();
            inv.callback().search(((search != null)
                    && search.contains("Search Query...")) ? null
                    : search);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        BukkitTask task = InventoryEventHandler.tasksToQuit.get(event.getInventory());
        if (task != null) {
            task.cancel();
        }

        for (SearchInv inv : List.copyOf(InventoryEventHandler.inventoriesToHandle)) {
            if (event.getView() != inv.view()) {
                continue;
            }
            inv.view().getTopInventory().clear();
            if (event.getReason() == InventoryCloseEvent.Reason.PLAYER) {
                TaskManager.Sync.runLater(Fadah.getINSTANCE(), () -> inv.callback().search(null), 1L);
            }
            InventoryEventHandler.inventoriesToHandle.remove(inv);
        }
    }
}
