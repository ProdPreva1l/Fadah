package info.preva1l.fadah.config.old;

import com.google.common.collect.ImmutableList;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.config.BasicConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "unused"})
@Getter
@AllArgsConstructor
public enum Lang {
    //PREFIX("prefix", "&#9555FF&lAuction House"),

    //OWN_LISTING("errors.own-listing", "&cYou cannot buy your own listing!"),
    TOO_EXPENSIVE("errors.too-expensive", "&cYou cannot afford this item!"),
    INVENTORY_FULL("errors.inventory-full", "&cYou don't have any free room in your inventory!"),
    //DOES_NOT_EXIST("errors.does-not-exist", "&cThis listing is no longer for sale!"),
    //MUST_BE_PLAYER("errors.must-be-player", "&cOnly players can run this command!"),
    //PLAYER_NOT_FOUND("errors.player-not-found", "&c{0} has not joined before!"),
    //NO_PERMISSION("errors.no-permission", "&cYou do not have permission to execute this command!"),
    DATABASE_CONNECTING("errors.database-connecting", "&cDatabase not connected! Please Wait"),
    //BAD_USAGE("errors.bad-usage", "&cUsage: /{0}"),
    //NO_COMMAND("errors.no-command", "&cThis command does not exist!"),
    CANT_SELL("errors.cant-sell", "&cYou cannot sell this item!"),

    //AUCTION_DISABLED("fail.disabled", "&cThe Auction House is currently disabled!"),
    //MAX_LISTINGS("fail.max-listings", "&cYou have reached your max listings! ({0}/{1})"),
    //MAX_LISTING_PRICE("fail.listing-price.max", "&fPrice must be less than &a${0}"),
    //MIN_LISTING_PRICE("fail.listing-price.min", "&fPrice must be at least &a${0}"),
    //MUST_BE_NUMBER("fail.must-be-number", "&cThe price must be a number!"),
    //MUST_HOLD_ITEM("fail.must-hold-item", "&fYou must have an item in your hand to sell!"),
    ADVERT_EXPENSE("fail.advert-too-expensive", "&cYour advert failed to post because you did not have enough money!"),
    COOLDOWN("fail.cooldown", "&cPlease wait &f{0}&c!"),

    //HELP_COMMAND_HEADER("help-command.header", "&#9555FF&lAuctionHouse &eHelp"),
    //HELP_COMMAND_FORMAT("help-command.format", "&b/{0} &8&l| &f{1}"),

    NOTIFICATION_NEW_LISTING("notifications.listed", List.of(
            "&f------------------------------------------------",
            "&eYou have a successfully listed an item for sale!",
            "&fItem: &e{0}",
            "&fPrice: &a${1}",
            "&fExpires in: &6{2}",
            "&fActive Listings: &d{3}&f/&5{4}",
            "&fYou have been taxed: &9{5}% &7(&a${6}&7)",
            "&f------------------------------------------------"
    )),
    CANCELLED("notifications.cancelled", "&cListing Cancelled!"),
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
    NOTIFICATION_ADVERT("notifications.advert", List.of(
            "&f--------------------------------------------------",
            "&f{0} &ehas just made a new listing on the auction house!",
            "&fItem: &e{1}",
            "&fPrice: &a${2}",
            "&7(Click this message to view the listing!)",
            "&f--------------------------------------------------"
    )),

    //CATEGORY_SELECTED("category-selected", "&e&lSELECTED"),

    SORT_ASCENDING("sort.direction.ascending.normal", "Ascending (A-Z)"),
    SORT_ASCENDING_AGE("sort.direction.ascending.age", "Newest First"),
    SORT_ASCENDING_PRICE("sort.direction.ascending.price", "Most Expensive First"),
    SORT_DESCENDING("sort.direction.descending.normal", "Descending (Z-A)"),
    SORT_DESCENDING_AGE("sort.direction.descending.age", "Oldest First"),
    SORT_DESCENDING_PRICE("sort.direction.descending.price", "Cheapest First"),
    SORT_AGE_NAME("sort.type.age", "Sort By Listing Age"),
    SORT_ALPHABETICAL_NAME("sort.type.alphabetical", "Sort Alphabetically By Name"),
    SORT_PRICE_NAME("sort.type.price", "Sort By Listing Price"),

    ADVERT_POST("listing-advert.post", "Post Advert"),
    ADVERT_DONT_POST("listing-advert.dont-post", "No Advert"),

    MODE_BUY_IT_NOW("modes.buy-it-now", "BIN"),
    MODE_BIDDING("modes.biddable", "Bidding"),

    ACTIONS_LISTING_START("logging-actions.listing-start", "Listing Started"),
    ACTIONS_LISTING_PURCHASED("logging-actions.listing-purchased", "Listing Purchased"),
    ACTIONS_LISTING_SOLD("logging-actions.listing-sold", "Listing Sold"),
    ACTIONS_LISTING_CANCEL("logging-actions.listing-cancelled", "Listing Cancelled"),
    ACTIONS_LISTING_EXPIRE("logging-actions.listing-expired", "Listing Expired"),
    ACTIONS_LISTING_ADMIN_CANCEL("logging-actions.listing-cancelled-admin", "Listing Cancelled by Admins"),
    ACTIONS_EXPIRED_ITEM_CLAIM("logging-actions.expired-item-claimed", "Expired Listing Claimed"),
    ACTIONS_EXPIRED_ITEM_ADMIN_CLAIM("logging-actions.expired-item-claimed-admin", "Expired Listing Claimed by Admins"),
    ACTIONS_COLLECTION_BOX_CLAIM("logging-actions.collection-box-claimed", "Collection Box Item Claimed"),
    ACTIONS_COLLECTION_BOX_ADMIN_CLAIM("logging-actions.collection-box-claimed-admin", "Collection Box Item Claimed by Admins"),

//    ADMIN_RELOAD("admin.reload.message", "&aConfigs reloaded!"),
//    ADMIN_RELOAD_REMOTE("admin.reload.remote", "&aConfigs reloaded from remote server!"),
//    ADMIN_TOGGLE_MESSAGE("admin.toggle.message", "&fAuction House has been {0}&r&f!"),
//    ADMIN_TOGGLE_REMOTE("admin.toggle.remote", "&fAuction House has been {0}&r&f from a remote server!"),
//    ADMIN_TOGGLE_ENABLED("admin.toggle.enabled", "&a&lEnabled"),
//    ADMIN_TOGGLE_DISABLED("admin.toggle.disabled", "&c&lDisabled"),

    WORD_YOU("words.you", "you"),
    WORD_YOUR("words.your", "your"),
    WORD_NONE("words.none", "None"),
    ;

    private final String path;
    private final Object defaultValue;

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

    public String toCapital() {
        return StringUtils.capitalize(toString());
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
