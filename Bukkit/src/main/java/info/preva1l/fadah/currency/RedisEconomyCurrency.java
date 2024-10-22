package info.preva1l.fadah.currency;

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import lombok.Getter;
import org.bukkit.OfflinePlayer;

@Getter
public class RedisEconomyCurrency implements Currency {
    private final String id = "redis_economy";
    private final String requiredPlugin = "RedisEconomy";

    private RedisEconomyAPI api;

    @Override
    public String getName() {
        return Config.i().getCurrency().getRedisEconomy().getName();
    }

    private dev.unnm3d.rediseconomy.currency.Currency getCurrency() {
        return api.getCurrencyByName(Config.i().getCurrency().getRedisEconomy().getSubCurrency());
    }

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
        if (getCurrency() == null) {
            Fadah.getConsole().severe("-------------------------------------");
            Fadah.getConsole().severe("Cannot enable redis economy currency!");
            Fadah.getConsole().severe("No currency with name : " + Config.i().getCurrency().getRedisEconomy().getSubCurrency());
            Fadah.getConsole().severe("-------------------------------------");
            return false;
        }
        return true;
    }
}
