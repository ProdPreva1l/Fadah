package info.preva1l.fadah.filters;

import info.preva1l.fadah.config.Config;
import org.bukkit.inventory.ItemStack;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

public class Restrictions {
    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    public static boolean isRestrictedItem(ItemStack item) {
        for (String blacklist : Config.i().getBlacklists()) {
            List<String> lore = item.getItemMeta().getLore();
            if (lore == null) lore = new ArrayList<>();
            blacklist = blacklist
                    .replace("%material%", "\"" + item.getType() + "\"")
                    .replace("%name%","\"" + item.getItemMeta().getDisplayName() + "\"")
                    .replace("%amount%", item.getAmount() + "")
                    .replace("%lore%", "\"" + String.join("\n", lore) + "\"");

            boolean result;
            try {
                result = (Boolean) engine.eval(blacklist);
            } catch (ScriptException e) {
                e.printStackTrace();
                return true;
            }
            if (result) {
                return true;
            }
        }
        return false;
    }
}
