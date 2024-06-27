package info.preva1l.fadah.api;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public final class ListingEndEvent extends Event {
    private final HandlerList handlers = new HandlerList();
    private final ListingEndReason reason;

    public ListingEndEvent(ListingEndReason reason) {
        super();
        this.reason = reason;
    }
}
