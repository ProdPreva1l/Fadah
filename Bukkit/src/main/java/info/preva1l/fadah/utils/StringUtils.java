package info.preva1l.fadah.utils;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;

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

    private final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-fA-F])");

    /**
     * Colorize  a string.
     * @param text String with color codes or hex codes.
     * @return Colorized String
     */
    public String colorize(String text) {
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
}
