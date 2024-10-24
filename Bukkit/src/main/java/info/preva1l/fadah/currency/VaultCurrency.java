package info.preva1l.fadah.currency;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

@Getter
public class VaultCurrency implements Currency {
    private final String id = "vault";
    private final String requiredPlugin = "Vault";

    private Economy economy;

    @Override
    public String getName() {
        return Config.i().getCurrency().getVault().getName();
    }

    @Override
    public void withdraw(OfflinePlayer player, double amountToTake) {
        if (economy == null) {
            throw new RuntimeException("Vault has no compatible economy plugin.");
        }
        economy.withdrawPlayer(player, amountToTake);
    }

    @Override
    public void add(OfflinePlayer player, double amountToAdd) {
        if (economy == null) {
            throw new RuntimeException("Vault has no compatible economy plugin.");
        }
        economy.depositPlayer(player, amountToAdd);
    }


    @Override
    public double getBalance(OfflinePlayer player) {
        if (economy == null) {
            throw new RuntimeException("Vault has no compatible economy plugin.");
        }
        return economy.getBalance(player);
    }

    @Override
    public boolean preloadChecks() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Fadah.getConsole().severe("---------------------------------------------------------");
            Fadah.getConsole().severe("Cannot enable vault currency! No Economy Plugin Installed");
            Fadah.getConsole().severe("---------------------------------------------------------");
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }
}
