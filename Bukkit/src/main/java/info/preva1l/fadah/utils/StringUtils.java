package info.preva1l.fadah.utils;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String Formatting Helper.
 */
@UtilityClass
public class StringUtils {
    /**
     * Colorize a list. (Useful for lore)
     *
     * @param list List typeof String
     * @return Colorized List typeof String
     */
    public List<String> colorizeList(List<String> list) {
        if (list == null) return null;
        if (list.isEmpty()) return null;
        List<String> ret = new ArrayList<>();
        for (String line : list) ret.add(colorize(line));
        return ret;
    }

    /**
     * Converts MiniMessage to legacy colour codes.
     * @param message message with mini message formatting
     * @return string with legacy formatting (not colorized)
     */
    public String miniMessageToLegacy(String message) {
        message = message.replace("<dark_red>", "&4");
        message = message.replace("<red>", "&c");
        message = message.replace("<gold>", "&6");
        message = message.replace("<yellow>", "&e");
        message = message.replace("<dark_green>", "&2");
        message = message.replace("<green>", "&a");
        message = message.replace("<aqua>", "&b");
        message = message.replace("<dark_aqua>", "&3");
        message = message.replace("<dark_blue>", "&1");
        message = message.replace("<blue>", "&9");
        message = message.replace("<light_purple>", "&d");
        message = message.replace("<dark_purple>", "&5");
        message = message.replace("<white>", "&f");
        message = message.replace("<gray>", "&7");
        message = message.replace("<dark_gray>", "&8");
        message = message.replace("<black>", "&0");
        message = message.replace("<b>", "&l");
        message = message.replace("<bold>", "&l");
        message = message.replace("<obf>", "&k");
        message = message.replace("<obfuscated>", "&k");
        message = message.replace("<st>", "&m");
        message = message.replace("<strikethrough>", "&m");
        message = message.replace("<u>", "&n");
        message = message.replace("<underline>", "&n");
        message = message.replace("<i>", "&o");
        message = message.replace("<italic>", "&o");
        message = message.replace("<reset>", "&r");
        message = message.replace("<r>", "&r");


        Pattern pattern = Pattern.compile("<#[a-fA-F0-9]{6}>");
        Matcher match = pattern.matcher(message);
        String code = message;
        while (match.find()) {
            code = message.substring(match.start(), match.end());
            code = code.replace("<", "&");
            code = code.replace(">", "");
        }
        return message.replaceAll("<#[a-fA-F0-9]{6}>", code);
    }

    /**
     * Converts legacy colour codes to MiniMessage.
     * @param message message with legacy codes
     * @return string with mini modernMessage formatting (not colorized)
     */
    public String legacyToMiniMessage(String message) {
        message = message.replace("&4", "<dark_red>");
        message = message.replace("&c", "<red>");
        message = message.replace("&6", "<gold>");
        message = message.replace("&e", "<yellow>");
        message = message.replace("&2", "<dark_green>");
        message = message.replace("&a", "<green>");
        message = message.replace("&b", "<aqua>");
        message = message.replace("&3", "<dark_aqua>");
        message = message.replace("&1", "<dark_blue>");
        message = message.replace("&9", "<blue>");
        message = message.replace("&d", "<light_purple>");
        message = message.replace("&5", "<dark_purple>");
        message = message.replace("&f", "<white>");
        message = message.replace("&7", "<gray>");
        message = message.replace("&8", "<dark_gray>");
        message = message.replace("&0", "<black>");
        message = message.replace("&l", "<b>");
        message = message.replace("&k", "<obf>");
        message = message.replace("&m", "<st>");
        message = message.replace("&n", "<u>");
        message = message.replace("&o", "<i>");
        message = message.replace("&r", "<reset>");

        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher match = pattern.matcher(message);
        StringBuffer result = new StringBuffer();
        while (match.find()) {
            String code = match.group();
            String replacement = code.replace("&", "<") + ">";
            match.appendReplacement(result, replacement);
        }
        match.appendTail(result);
        return result.toString();
    }


    private final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-fA-F])");

    /**
     * Colorize  a string.
     * @param text String with color codes or hex codes.
     * @return Colorized String
     */
    public String colorize(String text) {
        text = miniMessageToLegacy(text);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(null, text);
        }
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();

        while(matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    /**
     * Strip color codes from a string. (Doesn't remove hex codes)
     *
     * @param str String with color codes.
     * @return String without color codes.
     */
    public String removeColorCodes(String str) {
        return str.replaceAll("&(?! ).", "");
    }

    /**
     * Formats a string into a component.
     *
     * @param message string with mini message formatted colours and or placeholders
     * @param args    arguments for {@link StringUtils#formatPlaceholders(String, Object...)}
     * @return formatted component
     */
    public String message(String message, Object... args) {
        message = formatPlaceholders(message, args);

        return colorize(message);
    }

    /**
     * Formats Strings with placeholders
     *
     * @param message message with placeholders: {index}
     * @param args    things to replace with
     * @return formatted string
     */
    public String formatPlaceholders(String message, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (!message.contains("{" + i + "}")) {
                continue;
            }

            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return message;
    }

    /**
     * Capitalizes the first letter in a string.
     * @param str String
     * @return Capitalized String
     */
    public String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Gets an item name from an item stack
     * @param item item stack
     * @return formatted item name
     */
    public String extractItemName(ItemStack item) {
        if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        if (item.getItemMeta() != null && item.getItemMeta().hasLocalizedName()) {
            return item.getItemMeta().getLocalizedName();
        }
        String[] split = item.getType().name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            builder.append(capitalize(s)).append(" ");
        }
        return builder.toString().trim();
    }
}
