package info.preva1l.fadah.config;

import com.google.common.collect.ImmutableList;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.DatabaseType;
import info.preva1l.fadah.utils.BasicConfig;
import info.preva1l.fadah.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public enum Config {
    MAX_LISTING_PRICE("max-listing-price", 1000000000d),

    DECIMAL_FORMAT("decimal-format", "#,###.00"),

    STRICT_CHECKS("strict-checks", false),

    DATABASE_TYPE("database.type", "MONGO"),
    DATABASE_URI("database.uri", "mongodb://username:password@127.0.0.1:27017/admin?retryWrites=true&readPreference=nearest"),
    DATABASE("database.database", "SMP"),

    REDIS_ENABLED("redis.enabled", false),
    REDIS_URI("redis.uri", "redis://username:password@127.0.0.1:6379/0"),
    REDIS_CHANNEL("redis.channel", "auctionhouse.cache"),
    ;

    private final String path;
    private final Object defaultValue;

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
            Fadah.getConsole().warning(StringUtils.formatPlaceholders("Database Type \"{0}\" does not exist! \n Defaulting to MongoDB", toString()));
            return DatabaseType.MONGO;
        }
        return databaseType;
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

    public Component toFormattedComponent() {
        String str = Fadah.getINSTANCE().getConfigFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.message(defaultValue.toString());
        }
        return StringUtils.message(str);
    }

    public Component toFormattedComponent(Object... replacements) {
        String str = Fadah.getINSTANCE().getConfigFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.message(defaultValue.toString(), replacements);
        }
        return StringUtils.message(str, replacements);
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
}
