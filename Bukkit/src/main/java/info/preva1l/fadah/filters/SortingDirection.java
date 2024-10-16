package info.preva1l.fadah.filters;

import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.StringUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SortingDirection {
    ASCENDING(Lang.i().getSort().getName().getAscending(), Lang.i().getSort().getAge().getAscending(), Lang.i().getSort().getPrice().getAscending()),
    DESCENDING(Lang.i().getSort().getName().getDescending(), Lang.i().getSort().getAge().getDescending(), Lang.i().getSort().getPrice().getDescending()),
    ;
    private final String alphaName;
    private final String ageName;
    private final String priceName;

    public String getAlphaName() {
        return StringUtils.colorize(alphaName);
    }

    public String getAgeName() {
        return StringUtils.colorize(ageName);
    }

    public String getPriceName() {
        return StringUtils.colorize(priceName);
    }
}
