package info.preva1l.fadah.listeners;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.utils.guis.InventoryEventHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public class PlayerListener implements Listener {
    @EventHandler
    public void joinListener(AsyncPlayerPreLoginEvent e) {
        if (!DatabaseManager.getInstance().isConnected()) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(Lang.i().getPrefix() + Lang.i().getErrors().getDatabaseLoading());
            return;
        }

        Fadah.getINSTANCE().loadPlayerData(e.getUniqueId());
    }

    @EventHandler
    public void leaveListener(PlayerQuitEvent e) {
        CollectionBoxCache.invalidate(e.getPlayer().getUniqueId());
        ExpiredListingsCache.invalidate(e.getPlayer().getUniqueId());
        HistoricItemsCache.invalidate(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        BukkitTask task = InventoryEventHandler.tasksToQuit.get(event.getInventory());
        if (task != null) {
            task.cancel();
        }
    }
}
