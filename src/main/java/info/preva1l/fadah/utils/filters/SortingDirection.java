package info.preva1l.fadah.utils.filters;

import info.preva1l.fadah.config.Lang;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortingDirection {
    ASCENDING(Lang.SORT_ASCENDING.toString(), Lang.SORT_ASCENDING_AGE.toString()),
    DESCENDING(Lang.SORT_DESCENDING.toString(), Lang.SORT_DESCENDING_AGE.toString()),
    ;
    private final String friendlyName;
    private final String ageName;
}
