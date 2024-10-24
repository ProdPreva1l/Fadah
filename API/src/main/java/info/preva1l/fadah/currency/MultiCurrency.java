package info.preva1l.fadah.currency;

import java.util.List;

public interface MultiCurrency {
    String getId();

    String getName();

    String getRequiredPlugin();

    List<Currency> getCurrencies();

    /**
     * Pre startup checks for the currency hook.
     *
     * @return true if the checks succeed false if they fail.
     */
    default boolean preloadChecks() {
        return true;
    }
}
