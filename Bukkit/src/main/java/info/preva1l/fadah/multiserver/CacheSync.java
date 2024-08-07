package info.preva1l.fadah.multiserver;

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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.Pool;

import java.security.InvalidParameterException;
import java.util.UUID;

// TODO: Rewrite entire multi-server system
@SuppressWarnings("unchecked")
public class CacheSync extends JedisPubSub {
    private static Pool<Jedis> jedisPool;

    public static void send(UUID listingUUID, boolean remove) {
        if (Fadah.getINSTANCE().getCacheSync() == null) return;
        JSONObject obj = new JSONObject();
        CacheType type;
        if (remove) {
            type = CacheType.LISTINGS_REMOVE;
        } else {
            type = CacheType.LISTINGS_ADD;
        }
        obj.put("cache_type", type.ordinal());
        obj.put("listing_uuid", listingUUID.toString());
        TaskManager.Async.run(Fadah.getINSTANCE(), () -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(Config.REDIS_CHANNEL.toString(), obj.toJSONString());
            }
        });
    }

    public static void send(CacheType cacheType, UUID playerUUID) {
        if (Fadah.getINSTANCE().getCacheSync() == null) return;
        JSONObject obj = new JSONObject();
        obj.put("cache_type", cacheType.ordinal());
        obj.put("player_uuid", playerUUID.toString());
        TaskManager.Async.run(Fadah.getINSTANCE(), () -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(Config.REDIS_CHANNEL.toString(), obj.toJSONString());
            }
        });
    }

    public static void send(UUID playerUUID, String message) {
        if (Fadah.getINSTANCE().getCacheSync() == null) return;
        JSONObject obj = new JSONObject();
        obj.put("cache_type", CacheType.NOTIFICATIONS.ordinal());
        obj.put("player_uuid", playerUUID.toString());
        obj.put("message", message);
        TaskManager.Async.run(Fadah.getINSTANCE(), () -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(Config.REDIS_CHANNEL.toString(), obj.toJSONString());
            }
        });
    }

    public static void send(CacheType cacheType) {
        switch (cacheType) {
            case RELOAD, TOGGLE -> {
                JSONObject obj = new JSONObject();
                obj.put("cache_type", cacheType.ordinal());
                TaskManager.Async.run(Fadah.getINSTANCE(), () -> {
                    try (Jedis jedis = jedisPool.getResource()) {
                        jedis.publish(Config.REDIS_CHANNEL.toString(), obj.toJSONString());
                    }
                });
            }
            default -> throw new InvalidParameterException("Incorrect details provided!");
        }
    }

    public void start() {
        TaskManager.Async.run(Fadah.getINSTANCE(), () -> {
            Fadah.getConsole().info("Connecting to Redis Pool...");
            try {
                final JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxIdle(0);
                config.setTestOnBorrow(true);
                config.setTestOnReturn(true);

                jedisPool = new JedisPool(config, Config.REDIS_HOST.toString(), Config.REDIS_PORT.toInteger(), 0, Config.REDIS_PASSWORD.toString());
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.subscribe(this, Config.REDIS_CHANNEL.toString());
                }
            } catch (JedisException e) {
                Fadah.getConsole().info("Redis Failed to Connect!");
                throw new RuntimeException(e);
            }
            Fadah.getConsole().info("Redis Connected Successfully!");
        });
    }

    public void destroy() {
        jedisPool.destroy();
        Fadah.getINSTANCE().setCacheSync(null);
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(Config.REDIS_CHANNEL.toString())) return;

        JSONObject obj;
        try {
            obj = (JSONObject) new JSONParser().parse(message);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        CacheType cacheType = CacheType.values()[Integer.parseInt(obj.getOrDefault("cache_type", 0).toString())];
        cacheType.handleMessage(obj);
    }

    @AllArgsConstructor
    public enum CacheType {
        LISTINGS_ADD {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID listingUUID = UUID.fromString(obj.get("listing_uuid").toString());
                long delay = Config.STRICT_CHECKS.toBoolean() ? 40L : 20L;

                TaskManager.Sync.runLater(Fadah.getINSTANCE(), () ->
                        DatabaseManager.getInstance().get(Listing.class, listingUUID)
                                .thenAccept(listing -> listing.ifPresent(ListingCache::addListing)), delay);
            }
        },
        LISTINGS_REMOVE {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID listingUUID = UUID.fromString(obj.get("listing_uuid").toString());

                Listing listing = ListingCache.getListing(listingUUID);
                Fadah.getConsole().info(listing.getId().toString());
                ListingCache.removeListing(listing);
            }
        },
        COLLECTION_BOX {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID playerUUID = UUID.fromString(obj.get("player_uuid").toString());

                DatabaseManager.getInstance().get(CollectionBox.class, playerUUID)
                        .thenAccept(var1 -> var1.ifPresent(list -> CollectionBoxCache.update(playerUUID, list.collectableItems())));
            }
        },
        EXPIRED_LISTINGS {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID playerUUID = UUID.fromString(obj.get("player_uuid").toString());

                DatabaseManager.getInstance().get(ExpiredItems.class, playerUUID)
                        .thenAccept(var1 -> var1.ifPresent(list -> ExpiredListingsCache.update(playerUUID, list.collectableItems())));
            }
        },
        NOTIFICATIONS {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID playerUUID = UUID.fromString(obj.get("player_uuid").toString());
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null) return;

                player.sendMessage(StringUtils.colorize(obj.get("message").toString()));
            }
        },
        RELOAD {
            @Override
            public void handleMessage(JSONObject obj) {
                Fadah.getINSTANCE().reload();
                Bukkit.getConsoleSender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.ADMIN_RELOAD_REMOTE.toFormattedString());
            }
        },
        TOGGLE {
            @Override
            public void handleMessage(JSONObject obj) {
                FastInvManager.closeAll(Fadah.getINSTANCE());
                boolean enabled = Fadah.getINSTANCE().getConfigFile().getBoolean("enabled");
                Fadah.getINSTANCE().getConfigFile().save();
                Fadah.getINSTANCE().getConfigFile().getConfiguration().set("enabled", !enabled);
                Fadah.getINSTANCE().getConfigFile().save();
                Fadah.getINSTANCE().getConfigFile().load();

                String toggle = enabled ? Lang.ADMIN_TOGGLE_DISABLED.toFormattedString() : Lang.ADMIN_TOGGLE_ENABLED.toFormattedString();
                Bukkit.getConsoleSender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.ADMIN_TOGGLE_REMOTE.toFormattedString(toggle));
            }
        },
        HISTORY {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID playerUUID = UUID.fromString(obj.get("player_uuid").toString());

                DatabaseManager.getInstance().get(History.class, playerUUID)
                        .thenAccept(history -> history.ifPresent(items -> HistoricItemsCache.update(playerUUID, items.collectableItems())));
            }
        };

        public abstract void handleMessage(JSONObject obj);
    }
}
