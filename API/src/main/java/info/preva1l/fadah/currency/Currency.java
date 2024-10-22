package info.preva1l.fadah.currency;

import org.bukkit.OfflinePlayer;

public interface Currency {
    String getId();

    String getName();

    String getRequiredPlugin();

    void withdraw(OfflinePlayer player, double amountToTake);

    void add(OfflinePlayer player, double amountToAdd);

    double getBalance(OfflinePlayer player);

    default boolean canAfford(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    /**
     * Pre startup checks for the currency hook.
     *
     * @return true if the checks succeed false if they fail.
     */
    default boolean preloadChecks() {
        return true;
    }
}
