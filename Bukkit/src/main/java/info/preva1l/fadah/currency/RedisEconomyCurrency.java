package info.preva1l.fadah.currency;

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.SubEconomy;
import lombok.Getter;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RedisEconomyCurrency implements MultiCurrency {
    private final String id = "redis_economy";
    private final String requiredPlugin = "RedisEconomy";
    private final List<Currency> currencies = new ArrayList<>();

    private RedisEconomyAPI api;

    @Override
    public String getName() {
        return Config.i().getCurrency().getRedisEconomy().getName();
    }

    @Override
    public boolean preloadChecks() {
        api = RedisEconomyAPI.getAPI();
        if (api == null) {
            Fadah.getConsole().severe("-------------------------------------");
            Fadah.getConsole().severe("Cannot enable redis economy currency!");
            Fadah.getConsole().severe("Plugin did not start correctly.");
            Fadah.getConsole().severe("-------------------------------------");
            return false;
        }
        for (SubEconomy eco : Config.i().getCurrency().getRedisEconomy().getCurrencies()) {
            Currency subCur = new SubCurrency(eco.economy(), eco.displayName(), requiredPlugin) {
                private final RedisEconomyAPI api = getApi();

                @Override
                public void withdraw(OfflinePlayer player, double amountToTake) {
                    getCurrency().withdrawPlayer(player, amountToTake);
                }

                @Override
                public void add(OfflinePlayer player, double amountToAdd) {
                    getCurrency().depositPlayer(player, amountToAdd);
                }

                @Override
                public double getBalance(OfflinePlayer player) {
                    return getCurrency().getBalance(player);
                }

                private dev.unnm3d.rediseconomy.currency.Currency getCurrency() {
                    return api.getCurrencyByName(eco.economy());
                }

                @Override
                public boolean preloadChecks() {
                    if (getCurrency() == null) {
                        Fadah.getConsole().severe("-------------------------------------");
                        Fadah.getConsole().severe("Cannot enable redis economy currency!");
                        Fadah.getConsole().severe("No currency with name: " + eco.economy());
                        Fadah.getConsole().severe("-------------------------------------");
                        return false;
                    }
                    return true;
                }
            };
            currencies.add(subCur);
        }
        return true;
    }
}
