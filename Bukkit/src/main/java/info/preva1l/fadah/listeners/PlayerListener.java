package info.preva1l.fadah.listeners;


import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.guis.InventoryEventHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

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
    public void onClose(InventoryCloseEvent event) {
        BukkitTask task = InventoryEventHandler.tasksToQuit.get(event.getInventory());
        if (task != null) {
            task.cancel();
        }
    }
}
