package info.preva1l.fadah.api;

import info.preva1l.fadah.records.Listing;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public final class ListingPurchaseEvent extends Event {
    private final HandlerList handlers = new HandlerList();
    private final Listing listing;
    private final Player buyer;

    public ListingPurchaseEvent(Listing listing, Player buyer) {
        super();
        this.listing = listing;
        this.buyer = buyer;
    }
}
