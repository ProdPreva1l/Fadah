package info.preva1l.fadah.multiserver;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.records.History;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.guis.FastInvManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public abstract class Broker {

    protected static final Object DUMMY_VALUE = new Object();

    protected final Fadah plugin;
    protected final Gson gson;
    protected final Cache<Integer, Object> cachedIds;

    protected Broker(@NotNull Fadah plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.cachedIds = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
    }

    protected void handle(@NotNull Message message) {
        switch (message.getType()) {
            case LISTING_ADD -> message.getPayload()
                    .getUUID().ifPresentOrElse(uuid -> {
                        DatabaseManager.getInstance().get(Listing.class, uuid)
                                .thenAccept(listing -> listing.ifPresent(ListingCache::addListing));
                        }, () -> {
                        throw new IllegalStateException("Listing add message received with no listing UUID!");
                    });

            case LISTING_REMOVE -> message.getPayload()
                    .getUUID().ifPresentOrElse(uuid -> {
                        Listing listing = ListingCache.getListing(uuid);
                        if (listing == null) {
                            throw new IllegalStateException("Listing remove message received, but we do not have the same listing?");
                        }
                        ListingCache.removeListing(listing);
                        }, () -> {
                        throw new IllegalStateException("Listing remove message received with no listing UUID!");
                    });

            case COLLECTION_BOX_UPDATE -> message.getPayload()
                    .getUUID().ifPresentOrElse(uuid -> {
                        DatabaseManager.getInstance().get(CollectionBox.class, uuid)
                                .thenAccept(var1 -> var1.ifPresent(list -> CollectionBoxCache.update(uuid, list.collectableItems())));
                        }, () -> {
                        throw new IllegalStateException("Collection box update message received with no player UUID!");
                    });

            case EXPIRED_LISTINGS_UPDATE -> message.getPayload()
                    .getUUID().ifPresentOrElse(uuid -> {
                        DatabaseManager.getInstance().get(ExpiredItems.class, uuid)
                                .thenAccept(var1 -> var1.ifPresent(list -> ExpiredListingsCache.update(uuid, list.collectableItems())));
                        }, () -> {
                        throw new IllegalStateException("Expired listings update message received with no player UUID!");
                    });

            case HISTORY_UPDATE -> message.getPayload()
                    .getUUID().ifPresentOrElse(uuid -> {
                        DatabaseManager.getInstance().get(History.class, uuid)
                                .thenAccept(history -> history.ifPresent(items -> HistoricItemsCache.update(uuid, items.collectableItems())));
                        }, () -> {
                        throw new IllegalStateException("History update message received with no player UUID!");
                    });

            case NOTIFICATION -> message.getPayload()
                    .getNotification().ifPresentOrElse(notification -> {
                        Player player = Bukkit.getPlayer(notification.getPlayer());
                        if (player == null) return;

                        player.sendMessage(StringUtils.colorize(notification.getMessage()));
                        }, () -> {
                        throw new IllegalStateException("Notification message received with no notification info!");
                    });

            case BROADCAST -> message.getPayload()
                    .getBroadcast().ifPresentOrElse(broadcast -> {
                        TaskManager.Async.run(Fadah.getINSTANCE(), () -> {
                            Component textComponent = MiniMessage.miniMessage().deserialize(StringUtils.legacyToMiniMessage(broadcast.getMessage()));
                            if (broadcast.getClickCommand() != null) {
                                textComponent = textComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, broadcast.getClickCommand()));
                            }
                            for (Player announce : Bukkit.getOnlinePlayers()) {
                                Fadah.getINSTANCE().getAdventureAudience().player(announce).sendMessage(textComponent);
                            }
                        });
                    }, () -> {
                        throw new IllegalStateException("Broadcast message received with no broadcast info!");
                    });

            case RELOAD -> {
                Fadah.getINSTANCE().reload();
                Lang.sendMessage(Bukkit.getConsoleSender(), Lang.i().getPrefix() + Lang.i().getCommands().getReload().getRemote());
            }

            case TOGGLE -> {
                FastInvManager.closeAll(Fadah.getINSTANCE());
                boolean enabled = Config.i().isEnabled();
                Config.i().setEnabled(!enabled);

                String toggle = enabled ? Lang.i().getCommands().getToggle().getDisabled() : Lang.i().getCommands().getToggle().getEnabled();
                Lang.sendMessage(Bukkit.getConsoleSender(), Lang.i().getPrefix() + Lang.i().getCommands().getToggle().getRemote()
                        .replace("%status%", toggle));
            }

            default -> throw new IllegalStateException("Unexpected value: " + message.getType());
        }
    }

    public abstract void connect();

    protected abstract void send(@NotNull Message message);

    public abstract void destroy();

    @Getter
    @AllArgsConstructor
    public enum Type {
        REDIS("Redis"),
        ;
        private final String displayName;
    }
}
