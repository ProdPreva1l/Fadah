package info.preva1l.fadah.multiserver;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
@AllArgsConstructor
@Builder
public class Message {
    @Expose @Builder.Default private Integer id = ThreadLocalRandom.current().nextInt(0, 999999 + 1);
    @Expose private Type type;
    @Expose private Payload payload;

    public void send(Broker broker) {
        if (broker == null) return;
        broker.cachedIds.put(id, Broker.DUMMY_VALUE);
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
