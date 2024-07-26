package info.preva1l.fadah.multiserver;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class Payload {
    @Nullable
    @Expose
    private UUID uuid;

    @Nullable
    @Expose
    private Notification notification;

    /**
     * Returns an empty cross-server message payload.
     *
     * @return an empty payload
     */
    @NotNull
    public static Payload empty() {
        return new Payload();
    }

    /**
     * Returns a payload containing a {@link UUID}.
     *
     * @param uuid the uuid to send
     * @return a payload containing the uuid
     */
    @NotNull
    public static Payload withUUID(@NotNull UUID uuid) {
        final Payload payload = new Payload();
        payload.uuid = uuid;
        return payload;
    }

    /**
     * Returns a payload containing a message and a recipient.
     *
     * @param playerUUID the player to send the message to
     * @param message the message to send
     * @return a payload containing the message
     */
    @NotNull
    public static Payload withNotification(@NotNull UUID playerUUID, @NotNull String message) {
        final Payload payload = new Payload();
        payload.notification = new Notification(playerUUID, message);
        return payload;
    }

    public Optional<UUID> getUUID() {
        return Optional.ofNullable(uuid);
    }

    public Optional<Notification> getNotification() {
        return Optional.ofNullable(notification);
    }
}
