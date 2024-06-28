package info.preva1l.fadah.multiserver;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.*;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
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
                        Fadah.getINSTANCE().getDatabase().getListing(listingUUID)
                                .thenAccept(ListingCache::addListing), delay);
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

                Fadah.getINSTANCE().getDatabase().getCollectionBox(playerUUID)
                        .thenAccept(items -> CollectionBoxCache.update(playerUUID, items));
            }
        },
        EXPIRED_LISTINGS {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID playerUUID = UUID.fromString(obj.get("player_uuid").toString());

                Fadah.getINSTANCE().getDatabase().getExpiredItems(playerUUID)
                        .thenAccept(items -> ExpiredListingsCache.update(playerUUID, items));
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
                FastInvManager.closeAll(Fadah.getINSTANCE());
                Fadah.getINSTANCE().getConfigFile().load();
                Fadah.getINSTANCE().getLangFile().load();
                Fadah.getINSTANCE().getMenusFile().load();
                Fadah.getINSTANCE().getCategoriesFile().load();
                CategoryCache.update();
                Fadah.getINSTANCE().getDatabase().loadListings();
                Bukkit.getConsoleSender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + "&aConfig reloaded from remote server!"));
            }
        },
        TOGGLE {
            @Override
            public void handleMessage(JSONObject obj) {
                boolean enabled = Fadah.getINSTANCE().getConfigFile().getBoolean("enabled");
                Fadah.getINSTANCE().getConfigFile().getConfiguration().set("enabled", !enabled);

                String message = Lang.PREFIX.toFormattedString() + StringUtils.colorize("&fAuction House has been ");
                message += (enabled ? StringUtils.colorize("&c&lDisabled") : StringUtils.colorize("&a&lEnabled"));
                message += StringUtils.colorize(" &ffrom remote server!");
                Fadah.getINSTANCE().getConfigFile().save();

                FastInvManager.closeAll(Fadah.getINSTANCE());

                Bukkit.getConsoleSender().sendMessage(message);
            }
        },
        HISTORY {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID playerUUID = UUID.fromString(obj.get("player_uuid").toString());

                Fadah.getINSTANCE().getDatabase().getHistory(playerUUID)
                        .thenAccept(items -> HistoricItemsCache.update(playerUUID, items));
            }
        };

        public abstract void handleMessage(JSONObject obj);
    }
}
