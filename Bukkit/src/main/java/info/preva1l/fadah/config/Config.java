package info.preva1l.fadah.config;

import com.google.common.collect.ImmutableList;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.DatabaseType;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.config.BasicConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public enum Config {
    MAX_LISTING_PRICE("listing-price.max", 1000000000d),
    MIN_LISTING_PRICE("listing-price.min", 1),
    DEFAULT_MAX_LISTINGS("default-max-listings", 3),
    DECIMAL_FORMAT("decimal-format", "#,###.00"),

    ADVERT_DEFAULT("listing-adverts.default", false),
    ADVERT_DEFAULT_PRICE("listing-adverts.price", 100.0),

    STRICT_CHECKS("strict-checks", false),

    HOOK_ECO_ITEMS("hooks.eco-items", false),

    MIGRATOR_ZAUCTIONHOUSE_CATEGORIES("migrators.z-auction-house.categories-to-migrate", List.of("Blocks", "Tools", "Weapons", "Potions", "Misc")),

    LOG_TO_FILE("log-to-file", true),

    DATABASE_TYPE("database.type", "SQLITE"),
    DATABASE_URI("database.uri", "jdbc:mysql://username:password@127.0.0.1:3306/Fadah"),
    DATABASE("database.database", "Fadah"),

    BROKER_ENABLED("broker.enabled", false),
    BROKER_TYPE("broker.type", "REDIS"),
    REDIS_HOST("broker.redis.host", "127.0.0.1"),
    REDIS_PORT("broker.redis.port", 6379),
    REDIS_PASSWORD("broker.redis.password", "password"),
    REDIS_CHANNEL("broker.redis.channel", "auctionhouse.cache"),
    ;

    private final String path;
    private final Object defaultValue;

    public static void loadDefault() {
        BasicConfig configFile = Fadah.getINSTANCE().getConfigFile();

        for (Config config : Config.values()) {
            String path = config.getPath();
            String str = configFile.getString(path);
            if (str.equals(path)) {
                configFile.getConfiguration().set(path, config.getDefaultValue());
            }
        }

        configFile.save();
        configFile.load();
    }

    public String toString() {
        String str = Fadah.getINSTANCE().getConfigFile().getString(path);
        if (str.equals(path)) {
            return defaultValue.toString();
        }
        return str;
    }

    public DatabaseType toDBTypeEnum() {
        DatabaseType databaseType;
        try {
            databaseType = DatabaseType.valueOf(toString());
        } catch (EnumConstantNotPresentException ex) {
            Fadah.getConsole().warning(StringUtils.formatPlaceholders("Database Type \"{0}\" does not exist! \n Defaulting to SQLite", toString()));
            return DatabaseType.SQLITE;
        }
        return databaseType;
    }

    public Broker.Type toBrokerType() {
        Broker.Type brokerType;
        try {
            brokerType = Broker.Type.valueOf(toString());
        } catch (EnumConstantNotPresentException ex) {
            Fadah.getConsole().warning(StringUtils.formatPlaceholders("Broker Type \"{0}\" does not exist! \n Defaulting to Redis", toString()));
            return Broker.Type.REDIS;
        }
        return brokerType;
    }

    public String toFormattedString() {
        String str = Fadah.getINSTANCE().getConfigFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.colorize(defaultValue.toString());
        }
        return StringUtils.colorize(str);
    }

    public String toFormattedString(Object... replacements) {
        String str = Fadah.getINSTANCE().getConfigFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.formatPlaceholders(StringUtils.colorize(defaultValue.toString()), replacements);
        }
        return StringUtils.colorize(StringUtils.formatPlaceholders(str, replacements));
    }

    public List<String> toStringList() {
        List<String> str = Fadah.getINSTANCE().getConfigFile().getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path)) {
            return (List<String>) defaultValue;
        }
        if (str.get(0).equals("null")) {
            return ImmutableList.of();
        }
        return str;
    }

    public List<String> toLore() {
        List<String> str = Fadah.getINSTANCE().getConfigFile().getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path)) {
            List<String> ret = new ArrayList<>();
            for (String line : (List<String>) defaultValue) ret.add(StringUtils.formatPlaceholders(line));
            return StringUtils.colorizeList(ret);
        }
        if (str.get(0).equals("null")) {
            return ImmutableList.of();
        }
        List<String> ret = new ArrayList<>();
        for (String line : str) ret.add(StringUtils.formatPlaceholders(line));
        return StringUtils.colorizeList(ret);
    }

    public List<String> toLore(Object... replacements) {
        List<String> str = Fadah.getINSTANCE().getConfigFile().getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path)) {
            List<String> ret = new ArrayList<>();
            for (String line : (List<String>) defaultValue) ret.add(StringUtils.formatPlaceholders(line, replacements));
            return StringUtils.colorizeList(ret);
        }
        if (str.get(0).equals("null")) {
            return ImmutableList.of();
        }
        List<String> ret = new ArrayList<>();
        for (String line : str) {
            ret.add(StringUtils.formatPlaceholders(line, replacements));
        }
        return StringUtils.colorizeList(ret);
    }

    public boolean toBoolean() {
        return Boolean.parseBoolean(toString());
    }

    public int toInteger() {
        return Integer.parseInt(toString());
    }

    public double toDouble() {
        return Double.parseDouble(toString());
    }
}
