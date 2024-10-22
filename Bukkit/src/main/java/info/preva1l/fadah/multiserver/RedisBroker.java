package info.preva1l.fadah.multiserver;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.utils.TaskManager;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.Pool;

import java.util.logging.Level;

/**
 * Redis Broker
 * Most of this code is from Williams <a href="https://github.com/WiIIiam278/HuskHomes/">HuskHomes</a>
 */
public final class RedisBroker extends Broker {
    private final Subscriber subscriber;
    private static String CHANNEL = "NONE";

    public RedisBroker(@NotNull Fadah plugin) {
        super(plugin);
        this.subscriber = new Subscriber(this);
    }

    @Blocking
    @Override
    public void connect() throws IllegalStateException {
        final Pool<Jedis> jedisPool = getJedisPool();
        try {
            jedisPool.getResource().ping();
        } catch (JedisException e) {
            throw new IllegalStateException("Failed to establish connection with Redis. "
                    + "Please check the supplied credentials in the config file", e);
        }

        subscriber.enable(jedisPool);
        Thread thread = new Thread(subscriber::subscribe, "fadah:redis_subscriber");
        thread.setDaemon(true);
        thread.start();
    }


    @Override
    protected void send(@NotNull Message message) {
        TaskManager.Async.run(plugin, () -> subscriber.send(message));
    }

    @Override
    @Blocking
    public void destroy() {
        subscriber.disable();
    }

    @NotNull
    private static Pool<Jedis> getJedisPool() {
        Config.Broker conf = Config.i().getBroker();
        final String password = conf.getPassword();
        final String host = conf.getHost();
        final int port = conf.getPort();
        CHANNEL = conf.getChannel();

        final JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(20);
        config.setMaxTotal(50);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        return password.isEmpty()
                ? new JedisPool(config, host, port, 0, false)
                : new JedisPool(config, host, port, 0, password, false);
    }

    @AllArgsConstructor
    private static class Subscriber extends JedisPubSub {
        private static final int RECONNECTION_TIME = 8000;

        private final RedisBroker broker;

        private Pool<Jedis> jedisPool;
        private boolean enabled;
        private boolean reconnected;

        private Subscriber(@NotNull RedisBroker broker) {
            this.broker = broker;
        }

        private void enable(@NotNull Pool<Jedis> jedisPool) {
            this.jedisPool = jedisPool;
            this.enabled = true;
        }

        @Blocking
        private void disable() {
            this.enabled = false;
            if (jedisPool != null && !jedisPool.isClosed()) {
                jedisPool.close();
            }
            this.unsubscribe();
        }

        @Blocking
        public void send(@NotNull Message message) {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(CHANNEL, broker.gson.toJson(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Blocking
        private void subscribe() {
            while (enabled && !Thread.interrupted() && jedisPool != null && !jedisPool.isClosed()) {
                try (Jedis jedis = jedisPool.getResource()) {
                    if (reconnected) {
                        Fadah.getConsole().info("Redis connection is alive again");
                    }

                    jedis.subscribe(this, CHANNEL);
                } catch (Throwable t) {
                    onThreadUnlock(t);
                }
            }
        }

        private void onThreadUnlock(@NotNull Throwable t) {
            if (!enabled) {
                return;
            }

            if (reconnected) {
                Fadah.getConsole().log(Level.WARNING, "Redis Server connection lost. Attempting reconnect in %ss..."
                        .formatted(RECONNECTION_TIME / 1000), t);
            }
            try {
                this.unsubscribe();
            } catch (Throwable ignored) {
            }

            if (!reconnected) {
                reconnected = true;
            } else {
                try {
                    Thread.sleep(RECONNECTION_TIME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public void onMessage(@NotNull String channel, @NotNull String encoded) {
            if (!channel.equals(CHANNEL)) {
                return;
            }
            final Message message;
            try {
                message = broker.gson.fromJson(encoded, Message.class);
            } catch (Exception e) {
                Fadah.getConsole().warning("Failed to decode message from Redis: " + e.getMessage());
                return;
            }

            try {
                broker.handle(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
