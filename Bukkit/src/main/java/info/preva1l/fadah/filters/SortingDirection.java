package info.preva1l.fadah.filters;

import info.preva1l.fadah.config.old.Lang;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortingDirection {
    ASCENDING(Lang.SORT_ASCENDING.toString(), Lang.SORT_ASCENDING_AGE.toString(), Lang.SORT_ASCENDING_PRICE.toString()),
    DESCENDING(Lang.SORT_DESCENDING.toString(), Lang.SORT_DESCENDING_AGE.toString(), Lang.SORT_DESCENDING_PRICE.toString()),
    ;
    private final String friendlyName;
    private final String ageName;
    private final String priceName;
}
