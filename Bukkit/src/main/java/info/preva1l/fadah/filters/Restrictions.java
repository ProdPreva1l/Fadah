package info.preva1l.fadah.filters;

import info.preva1l.fadah.config.Config;
import org.bukkit.inventory.ItemStack;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.List;

public class Restrictions {
    public static boolean isRestrictedItem(ItemStack item) {
        for (String blacklist : Config.i().getBlacklists()) {
            List<String> lore = item.getItemMeta().getLore();
            if (lore == null) lore = new ArrayList<>();
            blacklist = blacklist
                    .replace("%material%", "\"" + item.getType() + "\"")
                    .replace("%name%", "\"" + escape(item.getItemMeta().getDisplayName()) + "\"")
                    .replace("%amount%", String.valueOf(item.getAmount()))
                    .replace("%lore%", "\"" + escape(String.join("\n", lore)) + "\"");

            boolean result;
            try (Context cx = Context.enter()) {
                Scriptable scope = cx.initStandardObjects();
                result = (Boolean) cx.evaluateString(scope, blacklist, "Fadah", 1, null);
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }

            if (result) {
                return true;
            }
        }
        return false;
    }

    public static String escape(String input) {
        input = input.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\'", "\\\'")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("`", "\\`");
        return input;
    }
}