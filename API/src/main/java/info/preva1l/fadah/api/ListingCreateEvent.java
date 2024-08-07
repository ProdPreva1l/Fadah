package info.preva1l.fadah.api;

import info.preva1l.fadah.records.Listing;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public final class ListingCreateEvent extends PlayerEvent implements Cancellable {
    private final HandlerList handlers = new HandlerList();
    private final Listing listing;
    private boolean cancelled = false;
    private String cancelReason = "A 3rd Party Hook has cancelled the creation of this listing!";

    public ListingCreateEvent(@NotNull Player who, @NotNull Listing listing) {
        super(who);
        this.listing = listing;
    }
}
