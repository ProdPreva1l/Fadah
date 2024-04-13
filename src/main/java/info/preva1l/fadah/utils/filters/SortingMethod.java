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
            Comparator.comparingLong(Listing::creationDate).reversed(),
            Comparator.comparingLong(Listing::creationDate)
    ),
    ALPHABETICAL(
            Lang.SORT_ALPHABETICAL_NAME.toFormattedString(),
            new AlphabeticalComparator(),
            new AlphabeticalComparator().reversed()
    ),
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
            String check1 = o1.itemStack().hasItemMeta() ? (o1.itemStack().getItemMeta().hasDisplayName() ?
                    StringUtils.removeColorCodes(o1.itemStack().getItemMeta().getDisplayName()) : o1.itemStack().getType().name()) : o1.itemStack().getType().name();
            String check2 = o2.itemStack().hasItemMeta() ? (o2.itemStack().getItemMeta().hasDisplayName() ?
                    StringUtils.removeColorCodes(o2.itemStack().getItemMeta().getDisplayName()) : o2.itemStack().getType().name()) : o2.itemStack().getType().name();
            return check1.compareToIgnoreCase(check2);
        }
    }
}
