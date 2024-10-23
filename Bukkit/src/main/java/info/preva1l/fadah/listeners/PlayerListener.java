package info.preva1l.fadah.listeners;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.guis.InventoryEventHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerListener implements Listener {
    private final List<UUID> loading = new CopyOnWriteArrayList<>();

    @EventHandler
    public void joinListener(AsyncPlayerPreLoginEvent e) {
        if (!DatabaseManager.getInstance().isConnected()) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(StringUtils.colorize(Lang.i().getPrefix() + Lang.i().getErrors().getDatabaseLoading()));
            return;
        }

        loading.add(e.getUniqueId());
        Fadah.getINSTANCE().loadPlayerData(e.getUniqueId()).join();
        loading.remove(e.getUniqueId());
    }

    @EventHandler
    public void finalJoin(PlayerJoinEvent e) {
        if (loading.contains(e.getPlayer().getUniqueId())) {
            e.getPlayer().kickPlayer(StringUtils.colorize(Lang.i().getPrefix() + Lang.i().getErrors().getDatabaseLoading()));
        }
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
