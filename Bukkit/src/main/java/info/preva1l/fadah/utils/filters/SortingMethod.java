package info.preva1l.fadah.utils.filters;

import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

@Getter
@AllArgsConstructor
public enum SortingMethod {
    AGE(
            Lang.SORT_AGE_NAME.toFormattedString(),
            Comparator.comparingLong(Listing::getCreationDate).reversed(),
            Comparator.comparingLong(Listing::getCreationDate)
    ),
    ALPHABETICAL(
            Lang.SORT_ALPHABETICAL_NAME.toFormattedString(),
            new AlphabeticalComparator(),
            new AlphabeticalComparator().reversed()
    ),
    PRICE(
            Lang.SORT_PRICE_NAME.toFormattedString(),
            Comparator.comparingDouble(Listing::getPrice).reversed(),
            Comparator.comparingDouble(Listing::getPrice)
    )
    ;

    private final String friendlyName;
    private final Comparator<Listing> normalSorter;
    private final Comparator<Listing> reversedSorter;

    public Comparator<Listing> getSorter(@NotNull SortingDirection direction) {
        return switch (direction) {
            case ASCENDING -> normalSorter;
            case DESCENDING -> reversedSorter;
        };
    }

    public String getLang(@NotNull SortingDirection direction) {
        return switch (this) {
            case AGE -> direction.getAgeName();
            case ALPHABETICAL -> direction.getFriendlyName();
            case PRICE -> direction.getPriceName();
        };
    }

    public SortingMethod next() {
        int currentOrd = this.ordinal();
        if (currentOrd + 1 >= SortingMethod.values().length) return null;
        return SortingMethod.values()[currentOrd + 1];
    }

    public SortingMethod previous() {
        int currentOrd = this.ordinal();
        if (currentOrd - 1 < 0) return null;
        return SortingMethod.values()[currentOrd - 1];
    }

    private static class AlphabeticalComparator implements Comparator<Listing> {
        @Override
        public int compare(Listing o1, Listing o2) {
            String check1 = o1.getItemStack().hasItemMeta() ? (o1.getItemStack().getItemMeta().hasDisplayName() ?
                    StringUtils.removeColorCodes(o1.getItemStack().getItemMeta().getDisplayName()) : o1.getItemStack().getType().name()) : o1.getItemStack().getType().name();
            String check2 = o2.getItemStack().hasItemMeta() ? (o2.getItemStack().getItemMeta().hasDisplayName() ?
                    StringUtils.removeColorCodes(o2.getItemStack().getItemMeta().getDisplayName()) : o2.getItemStack().getType().name()) : o2.getItemStack().getType().name();
            return check1.compareToIgnoreCase(check2);
        }
    }
}
