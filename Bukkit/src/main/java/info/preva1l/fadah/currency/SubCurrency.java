package info.preva1l.fadah.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class SubCurrency implements Currency {
    private final String id;
    private final String name;
    private final String requiredPlugin;
}
