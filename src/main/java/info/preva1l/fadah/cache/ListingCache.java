package info.preva1l.fadah.cache;

import info.preva1l.fadah.records.Listing;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UtilityClass
public final class ListingCache {
    private final List<@NotNull Listing> listings = new ArrayList<>();

    public void addListing(@Nullable Listing listing) {
        if (listing == null) {
            return;
        }
        listings.add(listing);
    }

    public void removeListing(@NotNull Listing listing) {
        listings.remove(listing);
    }

    public Listing getListing(@NotNull UUID id) {
        return listings.stream().filter(listing -> listing.id() == id).findFirst().orElse(null);
    }

    public void purgeListings() {
        listings.clear();
    }

    public List<Listing> getListings() {
        return new ArrayList<>(listings);
    }

    public boolean playerHasListings(UUID uuid) {
        return !listings.stream().filter(listing -> listing.isOwner(uuid)).toList().isEmpty();
    }
}