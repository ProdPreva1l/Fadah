package info.preva1l.fadah.currency;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class CurrencyRegistry {
    public static final Currency VAULT = get("vault");
    public static final Currency REDIS_ECONOMY = get("redis_economy");

    private static Map<String, Currency> values = new ConcurrentHashMap<>();

    public static void register(Currency currency) {
        if (values == null) {
            values = new ConcurrentHashMap<>();
        }

        if (!currency.getRequiredPlugin().isEmpty()) {
            Plugin requiredPlugin = Bukkit.getPluginManager().getPlugin(currency.getRequiredPlugin());
            if (requiredPlugin == null || !requiredPlugin.isEnabled()) {
                Logger.getLogger("Fadah")
                        .severe("Tried enabling currency %s but the required plugin %s is not found/enabled!"
                        .formatted(currency.getId().toLowerCase(), currency.getRequiredPlugin()));
                return;
            }
        }

        currency.preloadChecks();

        values.put(currency.getId(), currency);
    }

    public static Currency get(String currencyCode) {
        if (values == null) {
            values = new ConcurrentHashMap<>();
        }
        return values.get(currencyCode.toLowerCase());
    }

    public static List<Currency> getCurrencies() {
        return values.values().stream().toList();
    }

    public static Currency getNext(Currency current) {
        List<Currency> currencyList = getCurrencies();
        if (currencyList.isEmpty() || currencyList.size() == 1) {
            return null;
        }
        int index = currencyList.indexOf(current);
        if (index == -1) {
            return null;
        }
        int nextIndex = (index + 1) % currencyList.size();
        return currencyList.get(nextIndex);
    }

    public static Currency getPrevious(Currency current) {
        List<Currency> currencyList = getCurrencies();
        if (currencyList.isEmpty() || currencyList.size() == 1) {
            return null;
        }
        int index = currencyList.indexOf(current);
        if (index == -1) {
            return null;
        }
        int previousIndex = (index - 1 + currencyList.size()) % currencyList.size();
        return currencyList.get(previousIndex);
    }
}
