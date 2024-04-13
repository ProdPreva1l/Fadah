package info.preva1l.fadah.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String Formatting Helper.
 */
public class StringUtils {
    /**
     * Colorize a list. (Useful for lore)
     * @param list List typeof String
     * @return Colorized List typeof String
     */
    public static List<String> colorizeList(List<String> list) {
        if (list == null) return null;
        if (list.isEmpty()) return null;
        List<String> ret = new ArrayList<>();
        for (String line : list) ret.add(colorize(line));
        return ret;
    }

    /**
     * Colorize  a string.
     * @param str String with color codes or hex codes.
     * @return Colorized String
     */
    public static String colorize(String str) {
        if (str == null) return null;
        Pattern unicode = Pattern.compile("\\\\u\\+[a-fA-F0-9]{4}");
        Matcher match = unicode.matcher(str);
        while (match.find()) {
            String code = str.substring(match.start(),match.end());
            str = str.replace(code,Character.toString((char) Integer.parseInt(code.replace("\\u+",""),16)));
            match = unicode.matcher(str);
        }
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        match = pattern.matcher(str);
        while (match.find()) {
            String color = str.substring(match.start(),match.end());
            str = str.replace(color, ChatColor.of(color.replace("&","")) + "");
            match = pattern.matcher(str);
        }
        return ChatColor.translateAlternateColorCodes('&',str);
    }

    /**
     * Strip color codes from a string. (Doesn't remove hex codes)
     * @param str String with color codes.
     * @return String without color codes.
     */
    public static String removeColorCodes(String str) {
        return str.replaceAll("&(?! ).", "");
    }

    /**
     * Capitalizes the first letter in a string.
     * @param str String
     * @return Capitalized String
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Converts legacy colour codes to MiniMessage
     * @param message message with legacy codes
     * @return string with mini message formatting (not colorized)
     */
    public static String legacyToMiniMessage(String message) {
        //Codes AGHHHH MANUAL CONVERSION
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
        message = message.replace("&d", "<purple>");
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

        //Hex
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher match = pattern.matcher(message);
        String code = message;
        while (match.find()) {
            code = message.substring(match.start(),match.end());
            code = code.replace("&", "<");
            code = code + ">";
        }
        return message.replaceAll("&#[a-fA-F0-9]{6}", code);
    }

    public static List<String> lore(List<String> lore, Object... args) {
        List<String> ret = new ArrayList<>();
        for (String line : lore) ret.add(message(line).toString());
        return ret;
    }

    /**
     * Formats a string into a component.
     * @param message string with mini message formatted colours and or placeholders
     * @param args arguments for {@link StringUtils#formatPlaceholders(String, Object...)}
     * @return formatted component
     */
    public static Component message(String message, Object... args) {
        message = legacyToMiniMessage(message);

        message = formatPlaceholders(message, args);

        return MiniMessage.miniMessage().deserialize(message);
    }

    /**
     * Formats Strings with placeholders
     * @param message message with placeholders: {index}
     * @param args things to replace with
     * @return formatted string
     */
    public static String formatPlaceholders(String message, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (!message.contains("{" + i + "}")) {
                continue;
            }

            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return message;
    }

    /**
     * If only Java had fucking extension methods I wouldn't need this class...
     * @param haystack The string to compare
     * @param needles The strings to check if they match the haystack, while ignoring case.
     * @return true if one of the needles matches, false otherwise.
     */
    public static boolean equalsIgnoreCaseAny(String haystack, String... needles) {
        if (needles.length < 1) {
            return false;
        }

        for (String needle : needles) {
            if (haystack.equalsIgnoreCase(needle)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Converts a Throwable to a string
     * @param throwable just an exception
     * @return string Formatted for a discord webhook
     */
    public static String stackTraceToString(Throwable throwable) {
        StackTraceElement[] elements = throwable.getStackTrace();
        StringBuilder builder = new StringBuilder("\n> " + throwable.getMessage() + ":\n");

        for (StackTraceElement element : elements) {
            builder.append('\n').append("`").append(element).append("`");
        }

        return builder.substring(1);
    }

}
