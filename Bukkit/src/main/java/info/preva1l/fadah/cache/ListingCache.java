package info.preva1l.fadah.cache;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.records.Listing;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public final class ListingCache {
    private Map<UUID, @NotNull Listing> listings = new ConcurrentHashMap<>();

    public void addListing(@Nullable Listing newListing) {
        if (newListing == null) {
            return;
        }
        listings.put(newListing.getId(), newListing);
    }

    public void removeListing(@NotNull Listing listing) {
        listings.remove(listing.getId());
    }

    public Listing getListing(@NotNull UUID id) {
        return listings.get(id);
    }

    public void update() {
        Map<UUID, Listing> temp = new ConcurrentHashMap<>();
        Fadah.getINSTANCE().getDatabase().getListings().thenAccept(listings -> listings.forEach(listing -> temp.put(listing.getId(), listing)));
        listings = temp;
    }

    public Map<UUID, Listing> getListings() {
        return Collections.unmodifiableMap(listings);
    }
}