package info.preva1l.fadah.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class CooldownManager {
    private final Map<UUID, Long> sortCooldowns = new ConcurrentHashMap<>();

    public void startCooldown(Cooldown type, Player user) {
        sortCooldowns.put(user.getUniqueId(), Instant.now().toEpochMilli() + type.length.toMillis());
    }

    public boolean hasCooldown(Cooldown type, Player user) {
        if (type == Cooldown.SORT) {
            if (sortCooldowns.containsKey(user.getUniqueId())) {
                if (sortCooldowns.get(user.getUniqueId()) <= Instant.now().toEpochMilli()) {
                    sortCooldowns.remove(user.getUniqueId());
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public String getCooldownString(Cooldown type, Player user) {
        if (type == Cooldown.SORT) {
            return TimeUtil.formatTimeUntil(sortCooldowns.get(user.getUniqueId()));
        }
        return "0s";
    }

    @Getter
    @AllArgsConstructor
    public enum Cooldown {
        SORT(Duration.of(1, ChronoUnit.SECONDS))
        ;

        private final Duration length;
    }
}
