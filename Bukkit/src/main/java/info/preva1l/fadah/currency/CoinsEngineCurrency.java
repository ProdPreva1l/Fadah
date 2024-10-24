package info.preva1l.fadah.currency;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.SubEconomy;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CoinsEngineCurrency implements MultiCurrency {
    private final String id = "coins_engine";
    private final String name = "Coins Engine";
    private final String requiredPlugin = "CoinsEngine";
    private final List<Currency> currencies = new ArrayList<>();

    @Override
    public boolean preloadChecks() {
        for (SubEconomy eco : Config.i().getCurrency().getCoinsEngine().getCurrencies()) {
            Currency subCur = new SubCurrency(id + "_" + eco.economy(), eco.displayName(), requiredPlugin) {
                @Override
                public void withdraw(OfflinePlayer player, double amountToTake) {
                    CoinsEngineAPI.addBalance(player.getUniqueId(), getCurrency(), amountToTake);
                }

                @Override
                public void add(OfflinePlayer player, double amountToAdd) {
                    CoinsEngineAPI.addBalance(player.getUniqueId(), getCurrency(), amountToAdd);
                }

                @Override
                public double getBalance(OfflinePlayer player) {
                    return CoinsEngineAPI.getBalance(player.getUniqueId(), getCurrency());
                }

                private su.nightexpress.coinsengine.api.currency.Currency getCurrency() {
                    return CoinsEngineAPI.getCurrency(eco.economy());
                }

                @Override
                public boolean preloadChecks() {
                    if (getCurrency() == null) {
                        Fadah.getConsole().severe("-------------------------------------");
                        Fadah.getConsole().severe("Cannot enable coins engine currency!");
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
