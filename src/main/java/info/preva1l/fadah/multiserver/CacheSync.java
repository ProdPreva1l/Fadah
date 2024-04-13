package info.preva1l.fadah.multiserver;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TaskManager;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.util.UUID;

public class CacheSync extends JedisPubSub {
    private static Jedis jedis;
    public void start() {
        TaskManager.Async.run(Fadah.getINSTANCE(), () -> {
            Fadah.getConsole().info("Connecting to Redis Pool...");
            try {
                jedis = new Jedis(Config.REDIS_URI.toString());
            } catch (JedisException e) {
                Fadah.getConsole().info("Redis Failed to Connect!");
                throw new RuntimeException(e);
            }
            Fadah.getConsole().info("Redis Connected Successfully!");
        });
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

        CacheType cacheType = CacheType.values()[((int) obj.getOrDefault("cache_type", 0))];
        cacheType.handleMessage(obj);
    }

    public static void send(UUID listingUUID) {
        if (Fadah.getINSTANCE().getCacheSync() == null) return;
        JSONObject obj = new JSONObject();
        obj.put("cache_type", CacheType.LISTINGS.ordinal());
        obj.put("listing_uuid", listingUUID.toString());
        jedis.publish(Config.REDIS_CHANNEL.toString(), obj.toJSONString());
    }

    public static void send(CacheType cacheType, UUID playerUUID) {
        if (Fadah.getINSTANCE().getCacheSync() == null) return;
        JSONObject obj = new JSONObject();
        obj.put("cache_type", cacheType.ordinal());
        obj.put("player_uuid", playerUUID.toString());
        jedis.publish(Config.REDIS_CHANNEL.toString(), obj.toJSONString());
    }

    public static void send(UUID playerUUID, String message) {
        if (Fadah.getINSTANCE().getCacheSync() == null) return;
        JSONObject obj = new JSONObject();
        obj.put("cache_type", CacheType.NOTIFICATIONS.ordinal());
        obj.put("player_uuid", playerUUID.toString());
        obj.put("message", message);
        jedis.publish(Config.REDIS_CHANNEL.toString(), obj.toJSONString());
    }

    @AllArgsConstructor
    public enum CacheType {
        LISTINGS() {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID listingUUID = UUID.fromString(obj.get("listing_uuid").toString());

                ListingCache.addListing(Fadah.getINSTANCE().getDatabase().getListing(listingUUID));
            }
        },
        COLLECTION_BOX {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID playerUUID = UUID.fromString(obj.get("player_uuid").toString());

                Fadah.getINSTANCE().getDatabase().loadCollectionBox(playerUUID);
            }
        },
        EXPIRED_LISTINGS {
            @Override
            public void handleMessage(JSONObject obj) {
                UUID playerUUID = UUID.fromString(obj.get("player_uuid").toString());

                Fadah.getINSTANCE().getDatabase().loadExpiredItems(playerUUID);
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
        }
        ;

        public abstract void handleMessage(JSONObject obj);
    }
}
