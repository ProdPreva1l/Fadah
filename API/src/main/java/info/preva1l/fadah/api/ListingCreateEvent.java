package info.preva1l.fadah.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Setter
@Getter
public final class ListingCreateEvent extends Event implements Cancellable {
    private final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private String cancelReason = "A 3rd Party Hook has cancelled the creation of this listing!";
}
