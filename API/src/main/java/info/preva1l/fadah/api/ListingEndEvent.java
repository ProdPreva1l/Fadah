package info.preva1l.fadah.api;

import info.preva1l.fadah.records.Listing;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public final class ListingEndEvent extends Event {
    private final HandlerList handlers = new HandlerList();
    private final ListingEndReason reason;
    private final Listing listing;

    public ListingEndEvent(Listing listing, ListingEndReason reason) {
        super(true);
        this.listing = listing;
        this.reason = reason;
    }
}
