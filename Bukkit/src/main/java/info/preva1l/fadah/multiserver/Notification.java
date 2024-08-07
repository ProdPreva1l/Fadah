package info.preva1l.fadah.multiserver;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Notification {
    @Expose private final UUID player;
    @Expose private final String message;
}
