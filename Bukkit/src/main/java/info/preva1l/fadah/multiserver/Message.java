package info.preva1l.fadah.multiserver;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Message {
    @Expose private Type type;
    @Expose private Payload payload;

    public void send(Broker broker) {
        if (broker == null) return;
        broker.send(this);
    }

    public enum Type {
        LISTING_ADD,
        LISTING_REMOVE,
        COLLECTION_BOX_UPDATE,
        EXPIRED_LISTINGS_UPDATE,
        HISTORY_UPDATE,

        NOTIFICATION,
        BROADCAST,
        RELOAD,
        TOGGLE,
    }
}
