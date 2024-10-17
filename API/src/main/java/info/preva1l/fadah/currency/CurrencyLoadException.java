package info.preva1l.fadah.currency;

public class CurrencyLoadException extends RuntimeException {
    public CurrencyLoadException(String currency, String reason) {
        super(currency + " failed to load: " + reason);
    }
}
