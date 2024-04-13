package info.preva1l.fadah.config;

import com.google.common.collect.ImmutableList;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.BasicConfig;
import info.preva1l.fadah.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Lang {
    PREFIX("prefix", "&#9555FF&lDXTRUS"),

    OWN_LISTING("errors.own-listing", "&cYou cannot buy your own listing!"),
    TOO_EXPENSIVE("errors.too-expensive", "&cYou cannot afford this item!"),
    INVENTORY_FULL("errors.inventory-full", "&cYou don't have any free room in your inventory!"),
    DOES_NOT_EXIST("errors.does-not-exist", "&cThis listing is no longer for sale!"),
    MUST_BE_PLAYER("errors.must-be-player", "&cOnly players can run this command!"),
    NO_PERMISSION("errors.no-permission", "&cYou do not have permission to execute this command!"),
    DATABASE_CONNECTING("errors.database-connecting", "&cDatabase not connected! Please Wait"),

    NOTIFICATION_NEW_SELL("notifications.new-sell", List.of(
            "&f----------------------------------------------",
            "&eYou have sold an item on the Auction House!",
            "&fItem: &e{0}",
            "&fMoney Made: &a${1}",
            "&f----------------------------------------------"
    )),
    NOTIFICATION_NEW_ITEM("notifications.new-item", List.of(
            "&f------------------------------------------",
            "&eYou have a new item in your collection box!",
            "&f             /ah redeem!",
            "&f------------------------------------------"
    )),

    SORT_ASCENDING("sort.ascending.normal", "Ascending (A-Z)"),
    SORT_ASCENDING_AGE("sort.ascending.age", "Newest First"),
    SORT_DESCENDING("sort.descending.normal", "Descending (Z-A)"),
    SORT_DESCENDING_AGE("sort.descending.age", "Oldest First"),
    SORT_AGE_NAME("sort.type.age", "Sort By Listing Age"),
    SORT_ALPHABETICAL_NAME("sort.type.alphabetical", "Sort Alphabetically By Name"),
    ;

    private final String path;
    private final Object defaultValue;

    public String toString() {
        String str = Fadah.getINSTANCE().getLangFile().getString(path);
        if (str.equals(path)) {
            return defaultValue.toString();
        }
        return str;
    }

    public String toFormattedString() {
        String str = Fadah.getINSTANCE().getLangFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.colorize(defaultValue.toString());
        }
        return StringUtils.colorize(str);
    }

    public String toFormattedString(Object... replacements) {
        String str = Fadah.getINSTANCE().getLangFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.formatPlaceholders(StringUtils.colorize(defaultValue.toString()), replacements);
        }
        return StringUtils.colorize(StringUtils.formatPlaceholders(str, replacements));
    }

    public Component toFormattedComponent() {
        String str = Fadah.getINSTANCE().getLangFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.message(defaultValue.toString());
        }
        return StringUtils.message(str);
    }

    public Component toFormattedComponent(Object... replacements) {
        String str = Fadah.getINSTANCE().getLangFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.message(defaultValue.toString(), replacements);
        }
        return StringUtils.message(str, replacements);
    }

    public List<String> toStringList() {
        List<String> str = Fadah.getINSTANCE().getLangFile().getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path)) {
            return (List<String>) defaultValue;
        }
        if (str.get(0).equals("null")) {
            return ImmutableList.of();
        }
        return str;
    }

    public List<String> toStringList(Object... replacements) {
        List<String> str = Fadah.getINSTANCE().getLangFile().getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path)) {
            List<String> ret = new ArrayList<>();
            for (String line : (List<String>) defaultValue) ret.add(StringUtils.formatPlaceholders(line, replacements));
            return ret;
        }
        if (str.get(0).equals("null")) {
            return ImmutableList.of();
        }
        List<String> ret = new ArrayList<>();
        for (String line : str) {
            ret.add(StringUtils.formatPlaceholders(line, replacements));
        }
        return ret;
    }


    public List<String> toLore() {
        List<String> str = Fadah.getINSTANCE().getLangFile().getStringList(path);
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
        List<String> str = Fadah.getINSTANCE().getLangFile().getStringList(path);
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
        BasicConfig configFile = Fadah.getINSTANCE().getLangFile();

        for (Lang config : Lang.values()) {
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
