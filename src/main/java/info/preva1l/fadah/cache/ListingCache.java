package info.preva1l.fadah.cache;

import info.preva1l.fadah.records.Listing;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
public final class ListingCache {
    private final List<Listing> listings = new ArrayList<>();

    public void addListing(Listing listing) {
        listings.add(listing);
    }

    public void removeListing(Listing listing) {
        listings.remove(listing);
    }

    public Listing getListing(UUID id) {
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